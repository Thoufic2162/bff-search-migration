package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class SubCategory {
	@JsonProperty("name")
	private String name;

	@JsonProperty("title")
	private String title;

	@JsonProperty("productId")
	private String productId;

	@JsonProperty("imageId")
	private String embedId;

	@JsonProperty("image")
	private String imageurl;

	@JsonProperty("url")
	private String categoryUrl;

}
