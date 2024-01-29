package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class SearchBaseDTO {
	private String state;
	private String name;
	private String dimensionName;
	private String url;
	private boolean seoUrl = true;
	@JsonIgnore
	private int sequence;
	private String baseUrl;
	private Integer index;
	private Integer textIndex;
}
