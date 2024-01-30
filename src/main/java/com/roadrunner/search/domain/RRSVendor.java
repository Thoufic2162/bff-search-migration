package com.roadrunner.search.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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