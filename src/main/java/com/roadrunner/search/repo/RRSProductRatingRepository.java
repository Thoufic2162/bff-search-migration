package com.roadrunner.search.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roadrunner.search.domain.RRSProductRating;

@Repository
public interface RRSProductRatingRepository extends JpaRepository<RRSProductRating, String> {
	RRSProductRating findByRatingId(String ratingId);
}
