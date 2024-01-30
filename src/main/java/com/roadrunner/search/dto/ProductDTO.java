package com.roadrunner.search.dto;

import java.sql.Clob;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
	private String productId;
	private Integer cartOnlyClubPrice;
	private String vendorName;
	private int gender;
	private String genderText;
	private String pgcSubCode;
	private String pgcCodeId;
	private String brand;
	private String displayName;
	private String description;
	private Clob videoEmbeddedCode;
	private Integer umapHideVip;
	private String defaultColor;

}
