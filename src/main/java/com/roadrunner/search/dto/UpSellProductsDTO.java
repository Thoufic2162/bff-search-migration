package com.roadrunner.search.dto;

import java.util.List;

import lombok.Data;

@Data
public class UpSellProductsDTO {
	private String title;

	List<RecommendationProductDTO> products;

	BRMetadata metadata;
}
