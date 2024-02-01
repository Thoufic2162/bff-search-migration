package com.roadrunner.search.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationProductDTO extends SearchBaseDTO {

	private String sku = "";

	private String sflSku = "";

	private String brand = "";

	private String category = "";

	private String pgccode = "";

	private boolean outlet;

	private boolean exclusive;

	private String imageUrl = "";

	private boolean cartOnlyClubPrice;

	private String gender = "";

	private String description = "";

	private String colorCode = "";

	private float rating;

	private int reviews;

	@JsonIgnore
	private boolean displayVideo;

	private String saleMessage;

	private List<PriceDTO> price;

	private List<ColorSkusDTO> colorsSkus = new ArrayList<>();

	private InventoryDTO inventory;

	@JsonIgnore
	private String specialPricing = "";

	@JsonIgnore
	private boolean umapHideVIP;

	@JsonIgnore
	private double lowestListPrice = 0.0;

	@JsonIgnore
	private double lowestSalePrice = 0.0;

	@JsonIgnore
	private double highestSalePrice = 0.0;

	@JsonIgnore
	private double lowestVIPPrice = 0.0;

	@JsonIgnore
	private double highestVIPPrice = 0.0;

	@JsonIgnore
	private double lowestUmapPrice = 0.0;

	@JsonIgnore
	private double highestUmapPrice = 0.0;

	@JsonIgnore
	private String isSku = "false";

	@JsonIgnore
	private String hasSkus = "false";

	@JsonIgnore
	private boolean hideMsrp;

	private boolean displayVipMessage;

	private boolean recommendedProduct;

	@SerializedName("imageId")
	private String imageId;

	private boolean sflSkuInStock;

	private boolean sflProductInStock;

	private String banner;

	public void setColorsSku(ColorSkusDTO colorsSku) {
		if (colorsSku == null) {
			return;
		}
		if (colorsSkus == null) {
			colorsSkus = new ArrayList<>();
		}
		boolean match = false;
		for (int i = 0; i < colorsSkus.size(); i++) {
			if (((ColorSkusDTO) colorsSkus.get(i)).getColorCode().equalsIgnoreCase(colorsSku.getColorCode())) {
				match = true;
				break;
			}
		}
		if (!match) {
			colorsSkus.add(colorsSku);
		}
	}
}
