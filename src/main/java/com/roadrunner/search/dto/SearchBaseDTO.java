package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties({ "sequence", "baseUrl", "index", "textIndex" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchBaseDTO {
	
	private String state;
	
	private String name;
	
	private String dimensionName;
	
	private String url;
	
	@JsonProperty("seoURL")
	private boolean seoUrl = true;
	
	private int sequence;
	
	private String baseUrl;
	
	private Integer index;
	
	private Integer textIndex;
}
