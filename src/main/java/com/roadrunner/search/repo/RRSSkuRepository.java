package com.roadrunner.search.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roadrunner.search.domain.RRSSku;

@Repository
public interface RRSSkuRepository extends JpaRepository<RRSSku, String> {
	RRSSku findBySkuId(String skuId);
}
