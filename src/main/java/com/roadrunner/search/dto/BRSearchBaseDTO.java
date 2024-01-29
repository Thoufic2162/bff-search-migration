package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
@JsonIgnoreProperties({"sequence", "baseUrl", "index", "textIndex"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BRSearchBaseDTO {

	private String state;

	protected String name;

	protected String dimensionName;

	protected String url;

	@SerializedName("seoURL")
	protected boolean seoUrl = true;

	private int sequence;

	private String baseUrl;

	protected Integer index;

	private Integer textIndex;

}
