package com.roadrunner.search.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.roadrunner.search.dto.BloomreachSearchResponseDTO;

@Service
public interface BloomreachSearchService {

	String bloomreachSearchApiCall(String reqURL);

	String bloomreachApiCall(String url);

	BloomreachSearchResponseDTO populateBloomreachResponse(String productId, List<String> productIds);

}
