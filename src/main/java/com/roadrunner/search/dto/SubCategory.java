package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class SubCategory {
	@SerializedName("name")
	private String name;

	@SerializedName("title")
	private String title;

	@SerializedName("productId")
	private String productId;

	@SerializedName("imageId")
	private String embedId;

	@SerializedName("image")
	private String imageurl;

	@SerializedName("url")
	private String categoryUrl;

}
