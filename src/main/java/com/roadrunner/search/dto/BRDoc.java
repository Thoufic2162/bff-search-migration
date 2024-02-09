package com.roadrunner.search.dto;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class BRDoc {
	@SerializedName("sale_price")
	private double salePrice;

	private String price;

	@SerializedName("gender_text")
	private String genderText;

	@SerializedName("shoe_type")
	private String shoeType;

	@SerializedName("fit_size")
	private String fitSize;

	@SerializedName("default_color")
	private String defaultColor;

	@SerializedName("racc_price")
	private String raccPrice;

	private String exclusive;

	@SerializedName("long_description")
	private String longDescription;

	@SerializedName("has_video")
	private String hasVideo;

	@SerializedName("sub_category")
	private String subCategory;

	@SerializedName("map_sale_price")
	private Double mapSalePrice;

	@SerializedName("webSubPgc")
	private List<String> webSubPgc = null;

	@SerializedName("ranking")
	private Double ranking;

	@SerializedName("display_name")
	private String displayName;

	@SerializedName("extended_drop_ship")
	private String extendedDropShip;

	@SerializedName("colorGroup")
	private String colorGroup;

	@SerializedName("widthGroup")
	private String widthGroup;

	@SerializedName("creation_date")
	private String creationDate;

	@SerializedName("drop_ship")
	private String dropShip;

	@SerializedName("vip_price")
	private String vipPrice;

	@SerializedName("reg_price")
	private double regPrice;

	@SerializedName("rac_price")
	private String racPrice;

	@SerializedName("Shoe_Size")
	private String shoeSize;

	@SerializedName("width")
	private String width;

	@SerializedName("quantity_on_hand")
	private String quantityOnHand;

	@SerializedName("Apparel_Size")
	private String apparelSize;

	@SerializedName("umap_price")
	private double umapPrice;

	@SerializedName("taxable")
	private String taxable;

	@SerializedName("pgc_code")
	private List<String> pgcCode;

	@SerializedName("map_price")
	private String mapPrice;

	@SerializedName("gender_code")
	private Double genderCode;

	@SerializedName("features_benefits")
	private String featuresBenefits;

	@SerializedName("shoe_cushion")
	private String shoeCushion;

	@SerializedName("umap_hide_vip")
	private String umapHideVip;

	@SerializedName("webPgc")
	private List<String> webPgc = null;

	@Expose
	private String description;

	@SerializedName("sale_price_range")
	private List<Double> salePriceRange = null;

	@Expose
	private String url;

	@SerializedName("price_range")
	private List<Double> priceRange = null;

	@SerializedName("thumb_image")
	private String thumbImage;

	private String pid;

	private String brand;

	private String title;

	private List<Variants> variants = null;

	private String outlet;

	@SerializedName("customer_rating")
	private float customerRating;

	@SerializedName("customer_reviews")
	private int customerReviews;

	@SerializedName("rrs_sale")
	private String sale;

	@SerializedName("latest_version_pid")
	private String latestVersionId;
}
