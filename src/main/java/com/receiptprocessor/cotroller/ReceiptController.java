package com.receiptprocessor.cotroller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.receiptprocessor.model.Receipt;
import com.receiptprocessor.repository.ReceiptRepository;
import com.receiptprocessor.service.PointsService;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {

    private final ReceiptRepository receiptRepository;
    private final PointsService pointsService;

    /**
     * Constructor-based dependency injection.
     *
     * @param receiptRepository Repository for managing receipt storage.
     * @param pointsService     Service for calculating receipt-based reward points.
     */
    public ReceiptController(ReceiptRepository receiptRepository, PointsService pointsService) {
        this.receiptRepository = receiptRepository;
        this.pointsService = pointsService;
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
}

