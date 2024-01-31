package com.roadrunner.search.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.roadrunner.search.dto.BloomreachSearchResponseDTO;

@Service
public interface BloomreachSearchService {

	String bloomreachSearchApiCall(String reqURL);

	BloomreachSearchResponseDTO populateBloomreachResponse(String productId);

	String bloomreachApiCall(String url);

	BloomreachSearchResponseDTO populateProductsFromBR(List<String> productIds);

}
