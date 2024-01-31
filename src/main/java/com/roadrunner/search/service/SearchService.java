package com.roadrunner.search.service;

import org.springframework.stereotype.Service;

import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.RelatedProductResponseDTO;

import jakarta.servlet.http.HttpServletRequest;

@Service
public interface SearchService {

	BloomreachSearchResultsDTO restProductSearch(String qUri, HttpServletRequest request);

	RelatedProductResponseDTO getRelatedProducts(String productId);

	RelatedProductResponseDTO getNewOutletProducts();

}
