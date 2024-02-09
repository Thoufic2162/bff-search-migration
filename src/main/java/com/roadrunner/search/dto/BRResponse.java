package com.roadrunner.search.dto;

import java.util.List;

import lombok.Data;

@Data
public class BRResponse {
	
	private Integer numFound;

	private Integer start;

	private List<BRDoc> docs = null;
}
