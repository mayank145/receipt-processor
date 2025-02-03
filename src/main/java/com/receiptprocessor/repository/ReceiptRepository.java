package com.receiptprocessor.repository;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.receiptprocessor.model.Receipt;

@Repository
public class ReceiptRepository {
    private final ConcurrentHashMap<String, Receipt> storage = new ConcurrentHashMap<>();

    public String saveReceipt(Receipt receipt) {
        String id = UUID.randomUUID().toString();
        storage.put(id, receipt);
        return id;
    }

    public Receipt getReceipt(String id) {
        return storage.get(id);
    }
}
