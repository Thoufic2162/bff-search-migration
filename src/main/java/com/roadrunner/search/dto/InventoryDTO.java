package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class InventoryDTO {

	private int stockLevel;

	private String availabilityStatus;

	private String inventoryMessage;

	private String availabilityDate;

	private boolean alertMessage = false;

	private boolean isOutOfStockSku = false;
}
