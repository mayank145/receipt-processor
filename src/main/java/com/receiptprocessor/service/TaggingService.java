package com.receiptprocessor.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.receiptprocessor.model.Receipt;
import org.springframework.stereotype.Service;

@Service
public class TaggingService {
	
	public List<String> generateTags(Receipt receipt){
		List<String> tags = new ArrayList<>();
		
		if (receipt == null) {
			return List.of("Invalid Receipt");
		}
		
		if (receipt.getRetailer() != null && receipt.getRetailer().replaceAll("[^a-zA-Z0-9]", "").length() > 10) {
			tags.add("Loyal Customer");
		}
		if (receipt.getTotal() != null && !receipt.getTotal().trim().isEmpty()) {
		
			try {
				double totalAmount = Double.parseDouble(receipt.getTotal());
				if (totalAmount > 100) {
					tags.add("Big Spender");
				} 
			} catch (NumberFormatException e) {
				
				throw new IllegalArgumentException("Invalid total amount format: " + receipt.getTotal());
			}
		} else {
			tags.add("Missing Total Amount");
		}
		
		if (receipt.getPurchaseDate() != null && !receipt.getPurchaseDate().trim().isEmpty()) {
			
			try {
				LocalDate date = LocalDate.parse(receipt.getPurchaseDate());
				if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
					tags.add("Weekend Shopper");
				}
						
			} catch (DateTimeParseException e) {
				
				throw new IllegalArgumentException("Invalid purchase date format: " + receipt.getPurchaseDate());
				
			}
		} else {
			tags.add("Missing Purchase Date");
		}
		
		return tags;
		
	}

}
