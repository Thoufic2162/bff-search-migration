package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LargeBrandFile {
	public String url;

	public String imageId;

	public LargeDetails details;
}
