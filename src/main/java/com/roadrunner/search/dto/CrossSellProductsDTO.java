package com.roadrunner.search.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class CrossSellProductsDTO {
	private String title;

	private List<RecommendationProductDTO> products;

	private BRMetadata metadata;
}
