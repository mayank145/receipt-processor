package com.receiptprocessor.model;

import lombok.Data;

@Data
public class Item {
    public Item(String shortDescription, String price) {
		super();
		this.shortDescription = shortDescription;
		this.price = price;
	}
	private String shortDescription;
    private String price;
	public String getShortDescription() {
		return shortDescription;
	}
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
}
