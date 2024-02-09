package com.roadrunner.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class ColorSkusDTO {
	private String sku;

	private String colorCode = "";

	private String imageUrl = "";

	private String quantityOnHand = "0";

	private String colorDescription = "";

	@JsonProperty("imageId")
	private String embedId;

	@JsonProperty("altImageId")
	private String altEmbedId;
}
