package com.roadrunner.search.dto;

import lombok.Data;

@Data
public class FacetData {
	private String name;

	private Integer count;

	private String start;

	private String end;
}
