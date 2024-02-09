package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class BRMetadata {

	private QueryDetail query;
	
	private MetaWidget widget;
}
