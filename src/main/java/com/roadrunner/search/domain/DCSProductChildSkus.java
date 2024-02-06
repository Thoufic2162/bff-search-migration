package com.roadrunner.search.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dcs_prd_chldsku")
@Getter
@Setter
public class DCSProductChildSkus {
	@Id
	private String productId;
	private String skuId;
}
