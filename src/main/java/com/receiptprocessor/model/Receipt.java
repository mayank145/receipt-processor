package com.receiptprocessor.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import lombok.Data;

@Data
public class Receipt {
    private String retailer;
    private String purchaseDate;
    private String purchaseTime;
    private String total;
    private List<Item> items = new ArrayList<>();
    private List<String> tags = new ArrayList<>(); 
	public String getRetailer() {
		return retailer;
	}
	public void setRetailer(String retailer) {
		this.retailer = retailer;
	}
	public String getPurchaseDate() {
		return purchaseDate;
	}
	public void setPurchaseDate(String purchaseDate) {
		this.purchaseDate = purchaseDate;
	}
	public String getPurchaseTime() {
		return purchaseTime;
	}
	public void setPurchaseTime(String purchaseTime) {
		this.purchaseTime = purchaseTime;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public List<Item> getItems() {
		return items;
	}
	public void setItems(List<Item> items) {
		this.items = items;
	}
	/**
     * Validates that all prices in the items list are non-negative.
     * 
     * @throws IllegalArgumentException if any price is negative.
     */
    public void validatePrices() {
        for (Item item : items) {
            try {
                double price = Double.parseDouble(item.getPrice());
                if (price < 0) {
                    throw new IllegalArgumentException("Item price cannot be negative: " + item.getPrice());
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid price format: " + item.getPrice());
            }
        }
    }

    /**
     * Validates that the purchase date is not in the future.
     * 
     * @throws IllegalArgumentException if the purchase date is in the future.
     */
    public void validatePurchaseDate() {
        LocalDate date = LocalDate.parse(this.purchaseDate);
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Purchase date cannot be in the future: " + this.purchaseDate);
        }
    }
	public List<String> getTags() {
		return tags ;
	}
	public void setTags(List<String> tags) {
		this.tags = tags != null ? tags : new ArrayList<>();;
	}
	public void addTag(String tag) {
		if (tag != null && !tag.isEmpty() && !tags.contains(tag)) {
            tags.add(tag);
        }
	}
}