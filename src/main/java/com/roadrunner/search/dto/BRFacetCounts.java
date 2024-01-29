package com.roadrunner.search.dto;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class BRFacetCounts {
	
	@SerializedName("facet_fields")
	private FacetFields facetFields;

	@SerializedName("facet_ranges")
	private FacetRanges facetRanges;
}
