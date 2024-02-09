package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LargeBrandImage {
	
	public int width;
	
	public int height;
	
	public LargeBrandFields fields;
}
