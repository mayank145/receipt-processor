package com.receiptprocessor;

import com.receiptprocessor.model.Item;
import com.receiptprocessor.model.Receipt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptValidationTest {
    private Receipt receipt;

    @BeforeEach
    void setUp() {
        receipt = new Receipt();
        receipt.setRetailer("SuperMart");
        receipt.setTotal("50.00");
        receipt.setPurchaseDate(LocalDate.now().toString());
        receipt.setItems(Arrays.asList(new Item("Milk", "2.50"), new Item("Bread", "1.99")));
    }

    @Test
    void testValidReceipt() {
        assertDoesNotThrow(receipt::validatePrices);
        assertDoesNotThrow(receipt::validatePurchaseDate);
    }

    @Test
    void testNegativePriceThrowsException() {
        receipt.setItems(Collections.singletonList(new Item("Eggs", "-3.50")));
        Exception exception = assertThrows(IllegalArgumentException.class, receipt::validatePrices);
        assertEquals("Item price cannot be negative: -3.50", exception.getMessage());
    }

    @Test
    void testInvalidPriceFormatThrowsException() {
        receipt.setItems(Collections.singletonList(new Item("Eggs", "abc")));
        Exception exception = assertThrows(IllegalArgumentException.class, receipt::validatePrices);
        assertEquals("Invalid price format: abc", exception.getMessage());
    }

    @Test
    void testFuturePurchaseDateThrowsException() {
        receipt.setPurchaseDate(LocalDate.now().plusDays(1).toString());
        Exception exception = assertThrows(IllegalArgumentException.class, receipt::validatePurchaseDate);
        assertEquals("Purchase date cannot be in the future: " + LocalDate.now().plusDays(1), exception.getMessage());
    }
}