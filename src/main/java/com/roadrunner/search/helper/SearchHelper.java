package com.roadrunner.search.helper;

import java.util.Optional;
import java.util.Properties;

import org.springframework.stereotype.Service;

import com.roadrunner.search.dto.BloomreachSearchResponseDTO;

import jakarta.servlet.http.HttpServletRequest;

@Service
public interface SearchHelper {

	String getBrSearchToCategoryUrl(String query);

	void populateClearRefUrl(HttpServletRequest request, String query, Optional<String> color,
			Optional<String> searchBrand, Optional<String> searchGender, String searchShoeSize,
			Optional<String> searchApparelSize);

	BloomreachSearchResponseDTO performSearch(Properties queryParams, HttpServletRequest request, boolean isSearch);

}
