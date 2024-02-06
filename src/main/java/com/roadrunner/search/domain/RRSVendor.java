package com.roadrunner.search.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rrs_vendor")
@Getter
@Setter
public class RRSVendor {
	@Id
	private String vendorId;
	private String vendorName;
}