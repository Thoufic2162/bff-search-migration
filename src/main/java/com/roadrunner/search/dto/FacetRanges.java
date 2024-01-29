package com.roadrunner.search.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class FacetRanges {
	@SerializedName("reg_price")
	private List<FacetData> regPriceRangeList = null;

	@SerializedName("customer_rating")
	private List<FacetData> customerRatingList = null;

}
