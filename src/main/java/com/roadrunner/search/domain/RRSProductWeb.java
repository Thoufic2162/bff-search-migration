package com.roadrunner.search.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rrs_product_web")
@Getter
@Setter
public class RRSProductWeb {
	@Id
	private String productId;
	private String webPgcCode;
	private String WebPgcSubCode;
	private String kidsCategory;
	private int vipExclusive;
	private Integer clearance ;
}
