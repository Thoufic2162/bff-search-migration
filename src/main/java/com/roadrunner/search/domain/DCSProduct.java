package com.roadrunner.search.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Table(name = "dcs_product")
@Getter
@Setter
@Entity
public class DCSProduct {
	@Id
	private String productId;
	private int version;
	private String displayName;
	private String description;
	private String brand;
}