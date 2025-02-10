package com.receiptprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.receiptprocessor.model.Item;
import com.receiptprocessor.model.Receipt;
import com.receiptprocessor.repository.ReceiptRepository;
import com.receiptprocessor.service.PointsService;
import com.receiptprocessor.cotroller.ReceiptController;

class InventoryManagementTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private PointsService pointsService;

    @InjectMocks
    private ReceiptController receiptController;

    private Receipt receipt;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        receipt = new Receipt();
        receipt.setRetailer("Test Store");
        receipt.setPurchaseDate("2024-02-01");
        receipt.setPurchaseTime("14:00");
        receipt.setTotal("50.00");
        receipt.setItems(Arrays.asList(
                new Item("Item A", "10.00"),
                new Item("Item B", "15.00")
        ));
    }

    @Test
    @DisplayName(" Update Inventory for Existing Receipt")
    void testUpdateInventory_ExistingReceipt() {
        // Mock repository behavior
        when(receiptRepository.getReceipt("123")).thenReturn(receipt);
        when(pointsService.calculatePoints(any())).thenReturn(60); // Mock recalculated points

        // Create updated items
        List<Item> updatedItems = Arrays.asList(
                new Item("Item X", "20.00"),
                new Item("Item Y", "30.00")
        );

        // Call the API method
        ResponseEntity<Map<String, Object>> response = receiptController.updateReceiptInventory("123", updatedItems);

        // Assertions
        assertEquals(200, response.getStatusCode().value());
        assertEquals(updatedItems, response.getBody().get("updatedItems"));
        assertEquals(60, response.getBody().get("updatedPoints"));

        // Verify repository interaction
        verify(receiptRepository, times(1)).getReceipt("123");
        verify(pointsService, times(1)).calculatePoints(receipt);
    }

    @Test
    @DisplayName(" Update Inventory for Non-Existing Receipt")
    void testUpdateInventory_NonExistingReceipt() {
        // Mock repository returning null
        when(receiptRepository.getReceipt("999")).thenReturn(null);

        // Call the API method
        ResponseEntity<Map<String, Object>> response = receiptController.updateReceiptInventory("999", List.of());

        // Assertions
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    @DisplayName("️ Update Inventory with Negative Price - Should Fail")
    void testUpdateInventory_NegativePrice() {
        // Mock repository behavior
        when(receiptRepository.getReceipt("123")).thenReturn(receipt);

        // Create items with a negative price
        List<Item> invalidItems = Arrays.asList(
                new Item("Invalid Item", "-5.00")
        );

        // Call the API method
        ResponseEntity<Map<String, Object>> response = receiptController.updateReceiptInventory("123", invalidItems);

        // Assertions
        assertEquals(400, response.getStatusCode().value());
        assertEquals("error", response.getBody().keySet().iterator().next());
    }
}
