package com.roadrunner.search.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
