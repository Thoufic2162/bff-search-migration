package com.roadrunner.search.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class BrandCategory {
	
	@JsonProperty("title")
	private String title;

	@JsonProperty("link")
	private String url;

	@JsonProperty("image")
	private String imageurl;

	@JsonProperty("shopAllUrl")
	private String shopAllUrl;

	@JsonProperty("categories")
	private List<Category> categories = null;

}
