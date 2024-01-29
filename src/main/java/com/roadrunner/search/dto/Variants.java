package com.roadrunner.search.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Variants {

	@SerializedName("Apparel_Size")
	private List<String> apparelSize = null;

	@SerializedName("Shoe_Size")
	private List<String> shoeSize = null;

	@SerializedName("width")
	private List<String> width = null;

	@SerializedName("sku_color")
	private String skuColor;

	@SerializedName("skuid")
	private String skuid;

	@SerializedName("sku_price")
	private double skuPrice;

	@SerializedName("display_name")
	private List<String> displayName = null;

	@SerializedName("extended_drop_ship")
	private List<String> extendedDropShip = null;

	@SerializedName("vip_price")
	private List<Double> vipPrice = null;

	@SerializedName("reg_price")
	private List<Double> regPrice = null;

	@SerializedName("taxable")
	private List<String> taxable = null;

	@SerializedName("rac_price")
	private List<Double> racPrice = null;

	@SerializedName("colorGroup")
	private List<String> colorGroup = null;

	@SerializedName("ref_color_code")
	private List<String> refColorCode = null;

	@SerializedName("creation_date")
	private List<String> creationDate = null;

	@SerializedName("drop_ship")
	private List<String> dropShip = null;

	@SerializedName("quantity_on_hand")
	private List<String> quantityOnHand = null;

	@SerializedName("widthGroup")
	private List<String> widthGroup = null;

	@SerializedName("umap_price")
	private List<Double> umapPrice = null;

	@SerializedName("sku_sale_price")
	private Double skuSalePrice;

	@SerializedName("flavor_value")
	private List<String> flavorValue;

	@SerializedName("embed_id")
	private List<String> embedId;

	@SerializedName("availability_date")
	private List<String> altEmbedId;
}
