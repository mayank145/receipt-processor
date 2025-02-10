package com.receiptprocessor.service;

import com.receiptprocessor.model.Receipt;
import com.receiptprocessor.repository.ReceiptRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class AnalyticsService {
    private final ReceiptRepository receiptRepository;
    private final PointsService pointsService;

    public AnalyticsService(ReceiptRepository receiptRepository, PointsService pointsService) {
        this.receiptRepository = receiptRepository;
        this.pointsService = pointsService;
    }

    /**
     * **Real-Time Analytics Calculation**
     * - Computes the total number of receipts processed.
     * - Calculates the average points per receipt.
     * - Finds the receipt with the highest total.
     *
     * @return A map containing the computed analytics.
     */
    public Map<String, Object> getAnalytics() {
        List<Receipt> receipts = new ArrayList<>(receiptRepository.getAllReceipts().values());

        int totalReceipts = receipts.size();
        double avgPoints = totalReceipts == 0 ? 0.0 :
                receipts.stream().mapToInt(pointsService::calculatePoints).average().orElse(0.0);

        Optional<Receipt> maxTotalReceipt = receipts.stream()
                .filter(r -> isValidNumber(r.getTotal())) // Ensure the total is valid
                .max(Comparator.comparingDouble(r -> Double.parseDouble(r.getTotal())));

        // Construct analytics map safely
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalReceipts", totalReceipts);
        analytics.put("averagePoints", avgPoints);
        
        if (maxTotalReceipt.isPresent()) {
            Receipt receipt = maxTotalReceipt.get();
            analytics.put("highestTotalReceipt", Map.of(
                    "id", receiptRepository.getReceiptId(receipt),
                    "total", receipt.getTotal(),
                    "points", pointsService.calculatePoints(receipt)
            ));
        } else {
            analytics.put("highestTotalReceipt", null); //  Avoids NullPointerException
        }

        return analytics;
    }

    /**
     * ** Validates if a number is a valid non-null positive numeric string.**
     * - Ensures the total is not null or empty.
     * - Checks if the total is a valid double.
     *
     * @param total The total amount as a string.
     * @return `true` if valid, `false` otherwise.
     */
    private boolean isValidNumber(String total) {
        if (total == null || total.isEmpty()) return false;
        try {
            double value = Double.parseDouble(total);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
