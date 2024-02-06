package com.roadrunner.search.dto;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LargeBrandFields {

	public String title;

	public String description;

	public LargeBrandFile file;

	public String name;

	public LargeBrandDesktop desktop;

	public LargeBrandImage image;

	public String link;

	public String textAlign;

	@JsonProperty("mobileSize")
	public String mobileSize;

	public ArrayList<LargeBrandItem> items = new ArrayList<LargeBrandItem>();

	public String size;
}
