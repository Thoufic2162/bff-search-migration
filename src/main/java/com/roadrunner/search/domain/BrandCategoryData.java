package com.roadrunner.search.domain;

import java.sql.Blob;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
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
