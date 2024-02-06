package com.roadrunner.search.domain;

import java.sql.Blob;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "BR_BRAND_API_DATA")
public class BrandCategoryData {
	@Id
	private String brandName;
	@Lob
	private Blob apiData;
}
