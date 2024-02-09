package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LargeDetails {
	
	public int size;
	
	public LargeBrandImage image;
}
