package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class LargeBrandCategories {

	public String name;

	public LargeBrandCategory category;

	public String shopAllText;

	public String shopAllTitle;

	public String shopAllLink;

	public LargeCarousel carousel;

	public int spacingStripe;

	@JsonIgnore
	public int order;

}
