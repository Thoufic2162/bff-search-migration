package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LargeBrandItem {

	public LargeBrandFields fields;

	public int order;
}
