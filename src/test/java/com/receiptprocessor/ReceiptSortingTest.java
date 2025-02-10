package com.receiptprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.receiptprocessor.model.Receipt;
import com.receiptprocessor.repository.ReceiptRepository;
import com.receiptprocessor.service.PointsService;
import com.receiptprocessor.service.ReceiptSortingService;

import static org.mockito.Mockito.*;

class ReceiptSortingServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private PointsService pointsService;

    @InjectMocks
    private ReceiptSortingService receiptSortingService;

    private Receipt receipt1, receipt2, receipt3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        receipt1 = new Receipt();
        receipt1.setPurchaseDate("2025-01-15"); // Newest date
        receipt1.setTotal("30.00");

        receipt2 = new Receipt();
        receipt2.setPurchaseDate("2024-06-10"); // Oldest date
        receipt2.setTotal("15.50");

        receipt3 = new Receipt();
        receipt3.setPurchaseDate("2024-12-01"); // Mid-date
        receipt3.setTotal("25.75");

        when(receiptRepository.getAllReceipts()).thenReturn(Map.of(
            "id1", receipt1,
            "id2", receipt2,
            "id3", receipt3
        ));

        when(receiptRepository.getReceiptId(receipt1)).thenReturn("id1");
        when(receiptRepository.getReceiptId(receipt2)).thenReturn("id2");
        when(receiptRepository.getReceiptId(receipt3)).thenReturn("id3");

        when(pointsService.calculatePoints(receipt1)).thenReturn(90);
        when(pointsService.calculatePoints(receipt2)).thenReturn(75);
        when(pointsService.calculatePoints(receipt3)).thenReturn(80);
    }

    @Test
    @DisplayName("Test Sorting by Total Amount (Ascending)")
    void testSortByTotal() {
        List<Map<String, Object>> sortedReceipts = receiptSortingService.getSortedReceipts("total");

        assertEquals("id2", sortedReceipts.get(0).get("id")); // Lowest total first (15.50)
        assertEquals("id3", sortedReceipts.get(1).get("id")); // (25.75)
        assertEquals("id1", sortedReceipts.get(2).get("id")); // Highest total last (30.00)
    }

    @Test
    @DisplayName("Test Sorting by Date (Descending)")
    void testSortByDate() {
        List<Map<String, Object>> sortedReceipts = receiptSortingService.getSortedReceipts("date");

        assertEquals("id1", sortedReceipts.get(0).get("id")); // Newest date first (2025-01-15)
        assertEquals("id3", sortedReceipts.get(1).get("id")); // (2024-12-01)
        assertEquals("id2", sortedReceipts.get(2).get("id")); // Oldest date last (2024-06-10)
    }

    @Test
    @DisplayName("Test Sorting by Points (Descending)")
    void testSortByPoints() {
        List<Map<String, Object>> sortedReceipts = receiptSortingService.getSortedReceipts("points");

        assertEquals("id1", sortedReceipts.get(0).get("id")); // Highest points first (90)
        assertEquals("id3", sortedReceipts.get(1).get("id")); // (80)
        assertEquals("id2", sortedReceipts.get(2).get("id")); // Lowest points last (75)
    }
}