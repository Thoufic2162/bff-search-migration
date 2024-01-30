package com.roadrunner.search.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
