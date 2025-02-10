package com.receiptprocessor.cotroller;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.receiptprocessor.model.Item;
import com.receiptprocessor.model.Receipt;
import com.receiptprocessor.repository.ReceiptRepository;
import com.receiptprocessor.service.AnalyticsService;
import com.receiptprocessor.service.PointsService;
import com.receiptprocessor.service.TaggingService;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {

    private final ReceiptRepository receiptRepository;
    private final PointsService pointsService;
    private final TaggingService taggingService;
    private final AnalyticsService analyticsService;
	/**
     * Constructor-based dependency injection.
     *
     * @param receiptRepository Repository for managing receipt storage.
     * @param pointsService     Service for calculating receipt-based reward points.
     * @param taggingService Tagging for customer tag as "Loyal Customer, Big Spender, Weekend Shopper"
     * @param analyticsService  Analytics fetch real time analytics on processed receipts
     */
    public ReceiptController(ReceiptRepository receiptRepository, PointsService pointsService, TaggingService taggingService,AnalyticsService analyticsService ) {
        this.receiptRepository = receiptRepository;
        this.pointsService = pointsService;
        this.taggingService = taggingService;
        this.analyticsService = analyticsService;
        
    }

    /**
     * Processes a receipt and returns a unique identifier for it.
     *
     * @param receipt The receipt submitted in the request body.
     * @return A response containing the generated receipt ID.
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, String>> processReceipt(@RequestBody Receipt receipt) {
        if (receipt == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid request. Receipt cannot be null."));
        }

        try {
            receipt.validatePrices();  // Validate non-negative item prices
            receipt.validatePurchaseDate();  // Validate purchase date is not in the future
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }

        String id = receiptRepository.saveReceipt(receipt);
        return ResponseEntity.ok(Collections.singletonMap("id", id));
    }


    /**
     * Retrieves the calculated points for a given receipt ID.
     *
     * @param id The unique identifier of the receipt.
     * @return A response containing the calculated points or a 404 if not found.
     */
    @GetMapping("/{id}/points")
    public ResponseEntity<Map<String, Integer>> getPoints(@PathVariable String id) {
        Receipt receipt = receiptRepository.getReceipt(id);
        if (receipt == null) {
            return ResponseEntity.notFound().build();
        }

        int points = pointsService.calculatePoints(receipt);
        return ResponseEntity.ok(Collections.singletonMap("points", points));
    }
    
    /**
     * Tags a receipt based on predefined condition.
     * 
     * @param id The unique identifier of the receipt
     * @return A response containing the assigned tags or a 404 if not found.
     */
    @PostMapping("/{id}/tag")
    public ResponseEntity<Map<String, Object>> tagReceipt(@PathVariable String id){
    	Receipt receipt = receiptRepository.getReceipt(id);
    	if (receipt == null) {
    		return ResponseEntity.notFound().build();
    	}
    	
    	List<String> tags = taggingService.generateTags(receipt);
    	receipt.getTags().addAll(tags);
    	
    	Map<String, Object> response = new HashMap<>();
    	response.put("id", id);
        response.put("tags", receipt.getTags());
        return ResponseEntity.ok(response);
    	
    }
    
    /**
     * Sorts receipts based on query parameter: total (ascending), date (descending), or points (descending).
     * 
     * @param criteria Sorting criteria: "total", "date", or "points".
     * @return A sorted list of receipts.
     */
    @GetMapping("/sort")
    public ResponseEntity<List<Map<String, Object>>> sortReceipts(@RequestParam String criteria) {
    	Collection<Receipt> receipts = receiptRepository.getAllReceipts().values();

        List<Map<String, Object>> sortedReceipts = receipts.stream()
            .sorted(getComparator(criteria))
            .map(receipt -> {
                Map<String, Object> receiptData = new HashMap<>();
                receiptData.put("id", receiptRepository.getReceiptId(receipt));
                receiptData.put("total", receipt.getTotal());
                receiptData.put("date", receipt.getPurchaseDate());
                receiptData.put("points", pointsService.calculatePoints(receipt));
                return receiptData;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(sortedReceipts);
    }

    /**
     * Determines the comparator to use for sorting receipts.
     *
     * @param criteria Sorting criteria: "total", "date", or "points".
     * @return A comparator for sorting receipts.
     */
    private Comparator<Receipt> getComparator(String criteria) {
        switch (criteria.toLowerCase()) {
            case "total":
                return Comparator.comparingDouble(receipt -> Double.parseDouble(receipt.getTotal()));
            case "date":
                return Comparator.comparing(Receipt::getPurchaseDate).reversed();
            case "points":
                return Comparator.comparingInt(pointsService::calculatePoints).reversed();
            default:
                throw new IllegalArgumentException("Invalid sorting criteria. Use 'total', 'date', or 'points'.");
        }
    }
    
    /**
     * ** Inventory Update: Updates a receipt's items and recalculates points.**
     * @param id The receipt ID.
     * @param items The updated list of items.
     * @return A response containing the updated receipt and recalculated points.
     */
    @PutMapping("/{id}/inventory/update")
    public ResponseEntity<Map<String, Object>> updateReceiptInventory(
            @PathVariable String id,
            @RequestBody List<Item> items
    ) {
        Receipt receipt = receiptRepository.getReceipt(id);
        if (receipt == null) {
            return ResponseEntity.notFound().build();
        }

        receipt.setItems(items);

        try {
            receipt.validatePrices(); // Ensure all item prices are valid
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        int updatedPoints = pointsService.calculatePoints(receipt);

        return ResponseEntity.ok(Map.of(
                "id", id,
                "updatedItems", items,
                "updatedPoints", updatedPoints
        ));
    }
    /**
     * **Real-Time Analytics Endpoint**
     * Retrieves analytics on receipts including:
     * - Total receipts processed
     * - Average points per receipt
     * - Receipt with the highest total
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        return ResponseEntity.ok(analyticsService.getAnalytics());
    }
}

