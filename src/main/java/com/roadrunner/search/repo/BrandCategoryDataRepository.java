package com.roadrunner.search.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roadrunner.search.domain.BrandCategoryData;

@Repository
public interface BrandCategoryDataRepository extends JpaRepository<BrandCategoryData, String> {
	BrandCategoryData findByBrandName(String brandName);
}