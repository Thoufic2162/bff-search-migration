package com.roadrunner.search.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class BrandCategory {
	
	@SerializedName("title")
	private String title;

	@SerializedName("link")
	private String url;

	@SerializedName("image")
	private String imageurl;

	@SerializedName("shopAllUrl")
	private String shopAllUrl;

	@SerializedName("categories")
	private List<Category> categories = null;

}
