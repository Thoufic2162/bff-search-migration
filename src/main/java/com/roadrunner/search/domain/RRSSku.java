package com.roadrunner.search.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
