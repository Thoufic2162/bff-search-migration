package com.roadrunner.search.dto;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class MediumBrandCategories {

	public String name;

	public ArrayList<Banner1x6> banner1x6 = new ArrayList<Banner1x6>();

	public int spacingStripe;

}
