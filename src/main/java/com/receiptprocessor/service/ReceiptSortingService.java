package com.receiptprocessor.service;

import com.receiptprocessor.model.Receipt;
import com.receiptprocessor.repository.ReceiptRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReceiptSortingService {

    private final ReceiptRepository receiptRepository;
    private final PointsService pointsService;

    public ReceiptSortingService(ReceiptRepository receiptRepository, PointsService pointsService) {
        this.receiptRepository = receiptRepository;
        this.pointsService = pointsService;
    }

    /**
     * Retrieves sorted receipts based on the given criteria.
     *
     * @param criteria The sorting criteria (total, date, or points).
     * @return A list of sorted receipts represented as a map.
     */
    public List<Map<String, Object>> getSortedReceipts(String criteria) {
        // Convert stored receipts from Map<String, Receipt> to List<Receipt>
        List<Receipt> allReceipts = new ArrayList<>(receiptRepository.getAllReceipts().values());

        // Apply sorting based on criteria
        allReceipts.sort(getComparator(criteria));

        return allReceipts.stream()
                .map(receipt -> {
                    // Retrieve receipt ID from repository
                    String receiptId = receiptRepository.getReceiptId(receipt);

                    // If the receipt ID is not found, skip processing
                    if (receiptId == null) {
                        return null;
                    }

                    // Construct response map
                    Map<String, Object> receiptData = new HashMap<>();
                    receiptData.put("id", receiptId);
                    receiptData.put("total", Optional.ofNullable(receipt.getTotal()).orElse("0.00"));
                    receiptData.put("date", Optional.ofNullable(receipt.getPurchaseDate()).orElse("N/A"));
                    receiptData.put("points", pointsService.calculatePoints(receipt));

                    return receiptData;
                })
                .filter(Objects::nonNull) // Remove null values
                .collect(Collectors.toList()); // Convert to List
    }

    /**
     * Returns a comparator based on the given sorting criteria.
     *
     * @param criteria The sorting criteria (total, date, points).
     * @return The appropriate comparator for sorting receipts.
     */
    private Comparator<Receipt> getComparator(String criteria) {
        return switch (criteria) {
            case "total" -> Comparator.comparingDouble(r -> parseDouble(r.getTotal())); // Ascending order
            case "date" -> Comparator.comparing(Receipt::getPurchaseDate).reversed(); // Descending order
            case "points" -> Comparator.comparingInt(pointsService::calculatePoints).reversed(); // Descending order
            default -> throw new IllegalArgumentException("Invalid sorting criteria: " + criteria);
        };
    }

    /**
     * Safely parses a double value from a string.
     *
     * @param value The string value representing a number.
     * @return The parsed double value or 0.0 if parsing fails.
     */
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
