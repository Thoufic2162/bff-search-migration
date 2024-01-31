package com.roadrunner.search.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.roadrunner.search.domain.SeoCategory;

@Repository
public interface SeoCategoryRepository extends JpaRepository<SeoCategory, String> {
	@Query("SELECT s from SeoCategory s")
	List<SeoCategory> getSeoCategory();
}
