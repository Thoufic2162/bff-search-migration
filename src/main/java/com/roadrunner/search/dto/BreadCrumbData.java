package com.roadrunner.search.dto;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class BreadCrumbData {

	@SerializedName("cat_id")
	private String catId;

	@SerializedName("cat_name")
	private String catName;

	private String crumb;

	@SerializedName("tree_path")
	private String treePath;

	private String count;
}
