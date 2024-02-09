package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties({ "sequence", "baseUrl", "index", "textIndex" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BRSearchBaseDTO {

	private String state;

	private String name;

	private String dimensionName;

	private String url;

	private int sequence;

	private String baseUrl;

	private Integer index;

	private Integer textIndex;

}
