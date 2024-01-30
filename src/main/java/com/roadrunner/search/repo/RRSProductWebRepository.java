package com.roadrunner.search.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roadrunner.search.domain.RRSProductWeb;

@Repository
public interface RRSProductWebRepository extends JpaRepository<RRSProductWeb, String> {
	RRSProductWeb findByProductId(String productId);
}
