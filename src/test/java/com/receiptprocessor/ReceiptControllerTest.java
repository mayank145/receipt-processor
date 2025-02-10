package com.receiptprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.receiptprocessor.model.Receipt;
import com.receiptprocessor.service.TaggingService;

class TaggingServiceTest {

    @InjectMocks
    private TaggingService taggingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test Loyal Customer Tag")
    void testLoyalCustomerTag() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("SuperMegaMart123"); // More than 10 alphanumeric characters

        List<String> tags = taggingService.generateTags(receipt);
        assertTrue(tags.contains("Loyal Customer"));
    }

    @Test
    @DisplayName("Test Big Spender Tag")
    void testBigSpenderTag() {
        Receipt receipt = new Receipt();
        receipt.setTotal("150.75"); // More than $100

        List<String> tags = taggingService.generateTags(receipt);
        assertTrue(tags.contains("Big Spender"));
    }

    @Test
    @DisplayName("Test Weekend Shopper Tag (Saturday)")
    void testWeekendShopperTag_Saturday() {
        Receipt receipt = new Receipt();
        receipt.setPurchaseDate("2025-02-08"); // Saturday

        List<String> tags = taggingService.generateTags(receipt);
        assertTrue(tags.contains("Weekend Shopper"));
    }

    @Test
    @DisplayName("Test Weekend Shopper Tag (Sunday)")
    void testWeekendShopperTag_Sunday() {
        Receipt receipt = new Receipt();
        receipt.setPurchaseDate("2025-02-09"); // Sunday

        List<String> tags = taggingService.generateTags(receipt);
        assertTrue(tags.contains("Weekend Shopper"));
    }

    @Test
    @DisplayName("Test No Tags Applied")
    void testNoTagsApplied() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("Store"); // Less than 10 characters
        receipt.setTotal("50.00"); // Less than $100
        receipt.setPurchaseDate("2025-02-05"); // Wednesday (not weekend)

        List<String> tags = taggingService.generateTags(receipt);
        assertEquals(0, tags.size()); // Should not apply any tags
    }
}
