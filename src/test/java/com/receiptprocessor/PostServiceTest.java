package com.receiptprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
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
    void testCalculatePoints_RetailerNamePoints() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("Target123");

        int points = pointsService.calculatePoints(receipt);
        assertEquals(9, points);  // "Target123" has 9 alphanumeric characters
    }

    @Test
    void testCalculatePoints_RoundNumberTotal() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("Store");
        receipt.setTotal("50.00");

        int points = pointsService.calculatePoints(receipt);
        assertEquals(50 + 5 + 25, points);  // 50 for round total + 5 for "Store"
    }

    @Test
    void testCalculatePoints_MultipleOf0_25() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("Walmart");
        receipt.setTotal("25.25");

        int points = pointsService.calculatePoints(receipt);
        assertEquals(7 + 25, points);  // 7 from retailer + 25 for being multiple of 0.25
    }

    @Test
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
        assertEquals(4 + 5, points);  // 4 for retailer + 5 for 2 items
    }

    @Test
    void testCalculatePoints_PurchaseDayOdd() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("Amazon");
        receipt.setPurchaseDate("2022-01-03");  // Odd day

        int points = pointsService.calculatePoints(receipt);
        assertEquals(6 + 6, points);  // 6 for retailer + 6 for odd day
    }

    @Test
    void testCalculatePoints_PurchaseTimeBetween2PM_4PM() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("eBay");
        receipt.setPurchaseTime("14:30");  // Between 2PM-4PM

        int points = pointsService.calculatePoints(receipt);
        assertEquals(4 + 10, points);  // 4 from retailer + 10 bonus
    }
}