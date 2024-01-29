package com.roadrunner.search.dto;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class ColorSkusDTO {
	private String sku;

	private String colorCode = "";

	private String imageUrl = "";

	private String quantityOnHand = "0";

	private String colorDescription = "";

	@SerializedName("imageId")
	private String embedId;

	@SerializedName("altImageId")
	private String altEmbedId;
}
