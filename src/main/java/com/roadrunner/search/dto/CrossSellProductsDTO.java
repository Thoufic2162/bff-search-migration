package com.roadrunner.search.dto;

import java.util.List;

import lombok.Data;

@Data
public class CrossSellProductsDTO {
	private String title;

	List<RecommendationProductDTO> products;

	BRMetadata metadata;
}
