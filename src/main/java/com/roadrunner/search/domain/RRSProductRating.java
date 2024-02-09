package com.roadrunner.search.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rrs_product_rating")
@Getter
@Setter
public class RRSProductRating {
	@Id
	private String ratingId;
	private double rating;
	private int reviews;
}
