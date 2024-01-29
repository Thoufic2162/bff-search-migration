package com.roadrunner.search.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roadrunner.search.domain.SeoContent;

@Repository
public interface SeoContentRepository extends JpaRepository<SeoContent, String> {
	SeoContent findBySeoUrl(String seoUrl);
}
