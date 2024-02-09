package com.roadrunner.search.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Table(name = "rrs_seo_cat_recs")
@Entity
@Data
public class SeoCategory {
	@Id
	private String id;
	private String webPgcCode;
	private String webPgcSubCode;
	private String webPgcCodeTarget;
	private String webPgcSubCodeTarget;
	private String gender;
	private String qty;
}