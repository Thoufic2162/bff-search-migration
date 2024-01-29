package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BloomreachSearchRefinementsDTO extends BRSearchBaseDTO{
	private int products;
	private String answerId;
	private String moreRefinementsName;
	private Integer min;
	private Integer max;
	private String sizeCode;
	private boolean seoUrl = false;
}
