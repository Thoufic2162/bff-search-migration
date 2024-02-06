package com.roadrunner.search.domain;

import java.sql.Clob;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "rrs_product")
public class RRSProduct {
	@Id
	private String productId;
	private Integer status;
	private String pgcSubCode;
	private String pgcCodeId;
	private String vendorId;
	private int gender;
	private String genderText;
	private Integer cartOnlyClubPrice;
	@Lob
	private Clob videoEmbeddedCode;
	private Integer umapHideVip;
	private String defaultColor;
}