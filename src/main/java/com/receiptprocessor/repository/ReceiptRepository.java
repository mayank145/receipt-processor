package com.receiptprocessor.repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;
import com.receiptprocessor.model.Receipt;

@Repository
public class ReceiptRepository {
    private final ConcurrentHashMap<String, Receipt> storage = new ConcurrentHashMap<>();

    /**
     * Saves a receipt and generates a unique ID.
     *
     * @param receipt The receipt to store.
     * @return The generated receipt ID.
     */
    public String saveReceipt(Receipt receipt) {
        String id = UUID.randomUUID().toString();
        storage.put(id, receipt);
        return id;
    }

    /**
     * Retrieves a receipt by its ID.
     *
     * @param id The receipt ID.
     * @return The corresponding receipt or null if not found.
     */
    public Receipt getReceipt(String id) {
        return storage.get(id);
    }

    /**
     * Retrieves all stored receipts.
     *
     * @return A map containing all receipt IDs and their corresponding receipts.
     */
    public Map<String, Receipt> getAllReceipts() {
        return storage;
    }

    /**
     * Retrieves the receipt ID from the stored map.
     *
     * @param receipt The receipt object.
     * @return The corresponding receipt ID or null if not found.
     */
    public String getReceiptId(Receipt receipt) {
        return storage.entrySet()
                      .stream()
                      .filter(entry -> entry.getValue().equals(receipt))
                      .map(Map.Entry::getKey) 
                      .findFirst()
                      .orElse(null);
    }
}
