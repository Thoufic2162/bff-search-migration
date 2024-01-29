package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.Expose;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LargeBrandCategories {

	@Expose
	public String name;

	@Expose
	public LargeBrandCategory category;

	@Expose
	public String shopAllText;

	@Expose
	public String shopAllTitle;

	@Expose
	public String shopAllLink;

	@Expose
	public LargeCarousel carousel;

	@Expose
	public int spacingStripe;

	public int order;

}
