package com.roadrunner.search.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class FacetFields {
	@SerializedName("color_groups")
	private List<Object> colorGroups = null;

	private List<FacetData> colorGroup = null;

	private List<FacetData> brand = null;

	private List<BreadCrumbData> category = null;

	private List<Object> sizes = null;

	private List<Object> colors = null;

	@SerializedName("fit_size")
	private List<FacetData> fitSize = null;

	private List<FacetData> widthGroup = null;

	@SerializedName("shoe_type")
	private List<FacetData> shoeType = null;

	@SerializedName("apparel_type")
	private List<FacetData> apparelType = null;

	private List<FacetData> webSubPgc = null;

	private List<FacetData> webPgc = null;

	@SerializedName("gender_text")
	private List<FacetData> genderText = null;

	@SerializedName("shoe_height")
	private List<FacetData> shoeHeight = null;

	@SerializedName("shoe_cushion")
	private List<FacetData> shoeCushion = null;

	@SerializedName("rrs_sale")
	private List<FacetData> sale = null;

	@SerializedName("Apparel_Size")
	private List<FacetData> apparelSize = null;

	@SerializedName("new")
	private List<FacetData> newArraivals = null;

	private List<FacetData> outlet = null;

	@SerializedName("outlet_basement")
	private List<FacetData> outletBasement = null;

	@SerializedName("sock_thickness")
	private List<FacetData> sockThickness = null;

	@SerializedName("sock_length")
	private List<FacetData> sockLength = null;

	@SerializedName("accessory_type")
	private List<FacetData> accessoryType = null;

	@SerializedName("Shoe_Size")
	private List<FacetData> shoeSize = null;

	@SerializedName("kids_type")
	private List<FacetData> kidsType = null;

	@SerializedName("newProduct")
	private List<FacetData> newProduct = null;

	@SerializedName("exclusive")
	private List<FacetData> exclusive = null;

	private List<FacetData> sports = null;

	@SerializedName("kids_gender")
	private List<FacetData> kidsGender = null;

	@SerializedName("injury_recovery")
	private List<FacetData> injuryRecovery = null;

	@SerializedName("injury_placement")
	private List<FacetData> injuryPlacement = null;
}
