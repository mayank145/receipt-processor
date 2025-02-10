package com.receiptprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.receiptprocessor.model.Item;
import com.receiptprocessor.model.Receipt;
import com.receiptprocessor.service.PointsService;

class PointsServiceTest {

    @InjectMocks
    private PointsService pointsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Retailer Name Points - One point per alphanumeric character")
    void testCalculatePoints_RetailerNamePoints() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("Target123"); // 9 alphanumeric characters

        int points = pointsService.calculatePoints(receipt);
        assertEquals(9, points);
    }

    @Test
    @DisplayName("Round Number Total - 50 points if total is whole number")
    void testCalculatePoints_RoundNumberTotal() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("Store");
        receipt.setTotal("50.00"); // Should qualify for both 50-point bonus and 25-points bonuses

        int points = pointsService.calculatePoints(receipt);
        assertEquals(50 + 25 + 5, points); // 50 for round total + 5 for "Store"
    }

    @Test
    @DisplayName("Multiple of 0.25 - 25 points bonus")
    void testCalculatePoints_MultipleOf0_25() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("Walmart");
        receipt.setTotal("25.25"); // Should qualify for 25-point bonus

        int points = pointsService.calculatePoints(receipt);
        assertEquals(7 + 25, points); // 7 from retailer + 25 for being multiple of 0.25
    }

    @Test
    @DisplayName("Item Count Points - 5 points for every 2 items")
    void testCalculatePoints_ItemPoints() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("Shop");
        List<Item> items = Arrays.asList(
                new Item("Bread", "2.50"),  // +5 points for 2 items
                new Item("Milk", "3.00"),
                new Item("Eggs", "4.00")   // +5 more points for 2 more items
        );
        receipt.setItems(items);

        int points = pointsService.calculatePoints(receipt);
        assertEquals(4 + 5, points); // 4 for retailer + 5 for 2 items
    }

    @Test
    @DisplayName("Item Description Bonus - Extra points for descriptions multiple of 3")
    void testCalculatePoints_ItemDescriptionBonus() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("Amazon");
        List<Item> items = Arrays.asList(
                new Item("Cheese Pizza", "10.00") // 12 characters (multiple of 3)
        );
        receipt.setItems(items);

        int points = pointsService.calculatePoints(receipt);
        assertEquals(6 + 2, points); // 6 for retailer + 2 (10.00 * 0.2 rounded up)
    }

    @Test
    @DisplayName("Odd Purchase Day - 6 point bonus")
    void testCalculatePoints_PurchaseDayOdd() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("Amazon");
        receipt.setPurchaseDate("2025-02-07"); // Odd day

        int points = pointsService.calculatePoints(receipt);
        assertEquals(6 + 6, points); // 6 for retailer + 6 for odd day
    }

    @Test
    @DisplayName("Purchase Time Bonus - 10 points for purchases between 2PM-4PM")
    void testCalculatePoints_PurchaseTimeBetween2PM_4PM() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("eBay");
        receipt.setPurchaseTime("14:30"); // Between 2PM-4PM

        int points = pointsService.calculatePoints(receipt);
        assertEquals(4 + 10, points); // 4 from retailer + 10 bonus
    }

    @Test
    @DisplayName("No Bonus Applied - Zero edge case")
    void testCalculatePoints_NoBonus() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("A"); // Single-letter retailer
        receipt.setTotal("5.37"); // Not a multiple of 0.25 or round number
        receipt.setPurchaseDate("2025-02-06"); // Even day (No 6-point bonus)
        receipt.setPurchaseTime("10:00"); // Not between 2PM-4PM
        receipt.setItems(Arrays.asList(
                new Item("Bread", "1.50") // Not a multiple of 3
        ));

        int points = pointsService.calculatePoints(receipt);
        assertEquals(1, points); // Only retailer points apply
    }
}
