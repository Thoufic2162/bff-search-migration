package com.roadrunner.search.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rrs_sku")
@Getter
@Setter
public class RRSSku {
	@Id
	private String skuId;
	private Double umapPrice;
}
