package com.roadrunner.search.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Category {
	@SerializedName("name")
	private String name;

	@SerializedName("title")
	private String title;

	@SerializedName("productId")
	private String productId;

	@SerializedName("image")
	private String imageurl;

	@SerializedName("imageId")
	private String embedId;

	@SerializedName("link")
	private String categoryUrl;

	@SerializedName("subCategories")
	private List<SubCategory> subCategories = null;
}
