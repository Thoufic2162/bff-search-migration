package com.roadrunner.search.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public class BrandCategoriesResponse {

	@JsonProperty("BrandName")
	private String brandName;

	@JsonProperty("brandType")
	private String brandType;

	@JsonProperty("enableDynamicBrand")
	private boolean enableDynamicBrand;

	@JsonProperty("BrandCategories")
	private List<BrandCategory> brandCategories;

	@JsonProperty("largeBrandCategories")
	private List<LargeBrandCategories> largeBrandCategories = null;

	@JsonProperty("mediumBrandCategories")
	private MediumBrandCategories mediumBrandCategories = null;

}
