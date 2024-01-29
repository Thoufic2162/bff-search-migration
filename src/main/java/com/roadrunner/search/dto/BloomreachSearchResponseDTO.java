package com.roadrunner.search.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BloomreachSearchResponseDTO {

	private BRResponse response;

	@SerializedName("facet_counts")
	private BRFacetCounts facetCounts;

	@SerializedName("did_you_mean")
	private List<Object> didYouMean = null;

	private String autoCorrectQuery;

	private BRMetadata metadata;

	private BRKeywordRedirect keywordRedirect;
}
