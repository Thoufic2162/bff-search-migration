package com.roadrunner.search.dto;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class BRKeywordRedirect {

	@SerializedName("redirected url")
	private String redirectUrl;

	@SerializedName("redirected query")
	private String redirectQuery;

	@SerializedName("original query")
	private String originalQuery;
}
