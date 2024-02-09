package com.roadrunner.search.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="dcs_price")
@Getter
@Setter
public class DCSPrice {
	@Id
	private String priceId;
	private String priceList;
	private String productId;
	private String skuId;
	private Double listPrice;
}
