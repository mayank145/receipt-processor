package com.receiptprocessor.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.receiptprocessor.model.Item;
import com.receiptprocessor.model.Receipt;

@Service
public class PointsService {

    // Predefined DateTimeFormatter for efficient parsing of time
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Calculates the total reward points for a given receipt based on various criteria.
     *
     * @param receipt The receipt object containing purchase details.
     * @return The total points awarded for the receipt.
     * @throws IllegalArgumentException if the receipt is null.
     */
    public int calculatePoints(Receipt receipt) {
        validateReceipt(receipt); // Ensure receipt is not null before processing

        int points = 0;
        points += calculateRetailerPoints(receipt.getRetailer());
        points += calculateTotalPoints(receipt.getTotal());
        points += calculateItemPoints(receipt.getItems());
        points += calculatePurchaseDayPoints(receipt.getPurchaseDate());
        points += calculatePurchaseTimePoints(receipt.getPurchaseTime());

        return points;
    }

    /**
     * Validates if the receipt is null.
     *
     * @param receipt The receipt to validate.
     * @throws IllegalArgumentException if the receipt is null.
     */
    private void validateReceipt(Receipt receipt) {
        if (receipt == null) {
            throw new IllegalArgumentException("Receipt cannot be null.");
        }
    }

    /**
     * Calculates points based on the retailer's name.
     * One point is awarded for each alphanumeric character in the retailer's name.
     *
     * @param retailer The retailer's name.
     * @return The number of points awarded.
     */
    private int calculateRetailerPoints(String retailer) {
        if (retailer == null || retailer.isEmpty()) return 0;
        return retailer.replaceAll("[^a-zA-Z0-9]", "").length();
    }

    /**
     * Calculates points based on the total amount.
     * - 50 points if the total is a round number (e.g., 10.00).
     * - 25 points if the total is a multiple of 0.25.
     *
     * @param total The total purchase amount as a string.
     * @return The number of points awarded.
     */
    private int calculateTotalPoints(String total) {
        if (total == null || total.isEmpty()) return 0;

        int points = 0;
        try {
            double totalAmount = Double.parseDouble(total);

            if (total.matches("\\d+\\.00")) {
                points += 50; // Round number bonus
            }
            if (totalAmount % 0.25 == 0) {
                points += 25; // Multiple of 0.25 bonus
            }
        } catch (NumberFormatException e) {
            // Log invalid total but continue processing (avoid crashing)
        }
        return points;
    }

    /**
     * Calculates points based on the purchased items.
     * - 5 points for every two items.
     * - If an item's description length is a multiple of 3, its price is multiplied by 0.2, and the result is rounded up.
     *
     * @param items The list of purchased items.
     * @return The number of points awarded.
     */
    private int calculateItemPoints(List<Item> items) {
        if (items == null || items.isEmpty()) return 0;

        int points = (items.size() / 2) * 5; // 5 points for every two items

        for (Item item : items) {
            if (item.getShortDescription() == null || item.getPrice() == null) continue;

            String desc = item.getShortDescription().trim();

            try {
                double price = Double.parseDouble(item.getPrice());
                if (desc.length() % 3 == 0) {
                    points += (int) Math.ceil(price * 0.2); // Bonus for descriptions of length multiple of 3
                }
            } catch (NumberFormatException e) {
                // Log invalid price but continue processing
            }
        }
        return points;
    }

    /**
     * Awards 6 points if the purchase date falls on an odd-numbered day.
     *
     * @param purchaseDate The purchase date as a string (YYYY-MM-DD).
     * @return The number of points awarded.
     */
    private int calculatePurchaseDayPoints(String purchaseDate) {
        if (purchaseDate == null || purchaseDate.isEmpty()) return 0;

        try {
            LocalDate date = LocalDate.parse(purchaseDate);
            return (date.getDayOfMonth() % 2 == 1) ? 6 : 0; // 6 points for odd purchase days
        } catch (Exception e) {
            return 0; // Invalid date format, no points awarded
        }
    }

    /**
     * Awards 10 points if the purchase time falls between 2:00 PM and 4:00 PM.
     *
     * @param purchaseTime The purchase time as a string (HH:mm).
     * @return The number of points awarded.
     */
    private int calculatePurchaseTimePoints(String purchaseTime) {
        if (purchaseTime == null || purchaseTime.isEmpty()) return 0;

        try {
            LocalTime time = LocalTime.parse(purchaseTime, TIME_FORMATTER);
            return (time.isAfter(LocalTime.of(14, 0)) && time.isBefore(LocalTime.of(16, 0))) ? 10 : 0;
        } catch (Exception e) {
            return 0; // Invalid time format, no points awarded
        }
    }
}