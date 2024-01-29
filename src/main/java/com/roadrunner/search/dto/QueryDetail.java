package com.roadrunner.search.dto;

import java.util.List;

import lombok.Data;

@Data
public class QueryDetail {
	private ModificationDTO modification;

	private List<Object> didYouMean = null;
}
