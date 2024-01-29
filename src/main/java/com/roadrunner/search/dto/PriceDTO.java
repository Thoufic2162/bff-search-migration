package com.roadrunner.search.dto;

import lombok.Data;

@Data
public class PriceDTO {
	private String label;

	private String amount;

	private String discount;

	private String vipDiscount;

	private String type;

	private String symbol;
}
