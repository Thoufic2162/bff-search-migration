package com.roadrunner.search.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Category {
	@JsonProperty("name")
	private String name;

	@JsonProperty("title")
	private String title;

	@JsonProperty("productId")
	private String productId;

	@JsonProperty("image")
	private String imageurl;

	@JsonProperty("imageId")
	private String embedId;

	@JsonProperty("link")
	private String categoryUrl;

	@JsonProperty("subCategories")
	private List<SubCategory> subCategories = null;
}
