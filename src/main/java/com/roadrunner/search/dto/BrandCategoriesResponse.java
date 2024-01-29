package com.roadrunner.search.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public class BrandCategoriesResponse {

	@SerializedName("BrandName")
	private String brandName;

	@SerializedName("brandType")
	private String brandType;

	@SerializedName("enableDynamicBrand")
	private boolean enableDynamicBrand;

	@SerializedName("BrandCategories")
	private List<BrandCategory> brandCategories;

	@SerializedName("largeBrandCategories")
	private List<LargeBrandCategories> largeBrandCategories = null;

	@SerializedName("mediumBrandCategories")
	private MediumBrandCategories mediumBrandCategories = null;

}
