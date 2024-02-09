package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class MediumBrandFields {

	public String title;

	public String description;

	public MediumBrandFile file;

	public String name;

	public MediumBrandDesktop desktop;

	public MediumBrandImage image;

	public String link;

	public String textAlign;

}
