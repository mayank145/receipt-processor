package com.receiptprocessor;

import com.receiptprocessor.model.Receipt;
import com.receiptprocessor.repository.ReceiptRepository;
import com.receiptprocessor.service.AnalyticsService;
import com.receiptprocessor.service.PointsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyticsServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private PointsService pointsService;

    @InjectMocks
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAnalytics_NoReceipts() {
//        when(receiptRepository.getAllReceipts()).thenReturn(Map.of());

        Map<String, Object> analytics = analyticsService.getAnalytics();

        assertEquals(0, analytics.get("totalReceipts"));
        assertEquals(0.0, analytics.get("averagePoints"));
        assertNull(analytics.get("highestTotalReceipt"));
    }

    @Test
    void testGetAnalytics_WithReceipts() {
        Receipt receipt1 = new Receipt();
        receipt1.setTotal("30.50");

        Receipt receipt2 = new Receipt();
        receipt2.setTotal("50.00");

        when(receiptRepository.getAllReceipts()).thenReturn(Map.of(
                "1", receipt1,
                "2", receipt2
        ));

        when(pointsService.calculatePoints(receipt1)).thenReturn(40);
        when(pointsService.calculatePoints(receipt2)).thenReturn(60);
        when(receiptRepository.getReceiptId(receipt2)).thenReturn("2");

        Map<String, Object> analytics = analyticsService.getAnalytics();

        assertEquals(2, analytics.get("totalReceipts"));
        assertEquals(50.0, analytics.get("averagePoints"));
        assertNotNull(analytics.get("highestTotalReceipt"));
        @SuppressWarnings("unchecked")
        Map<String, Object> highestTotalReceipt = (Map<String, Object>) analytics.get("highestTotalReceipt");
        assertEquals("2", highestTotalReceipt.get("id"));
        assertEquals("50.00", highestTotalReceipt.get("total"));
        assertEquals(60, highestTotalReceipt.get("points"));
    }
}
