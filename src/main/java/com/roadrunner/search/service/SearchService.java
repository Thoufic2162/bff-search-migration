package com.roadrunner.search.service;

import org.springframework.stereotype.Service;

import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.RelatedProductResponseDTO;
import com.roadrunner.search.dto.response.BaseResponseDTO;

import jakarta.servlet.http.HttpServletRequest;

@Service
public interface SearchService {

	BaseResponseDTO<BloomreachSearchResultsDTO> restProductSearch(String qUri, HttpServletRequest request);

	BaseResponseDTO<RelatedProductResponseDTO> getRelatedProducts(String productId, String page);

	BaseResponseDTO<RelatedProductResponseDTO> getNewOutletProducts();

}
