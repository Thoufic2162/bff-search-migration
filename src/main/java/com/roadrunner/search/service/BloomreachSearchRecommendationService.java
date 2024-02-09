package com.roadrunner.search.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.roadrunner.search.dto.CrossSellProductsDTO;
import com.roadrunner.search.dto.ProductDTO;
import com.roadrunner.search.dto.RecommendationProductDTO;
import com.roadrunner.search.dto.UpSellProductsDTO;

@Service
public interface BloomreachSearchRecommendationService {

	List<RecommendationProductDTO> searchRecommendationsForUpSellAndCrossSell(Object profile,
			Map<String, String> refParams, UpSellProductsDTO upSellProductsDTO,
			CrossSellProductsDTO crossSellProductsDTO);

	List<RecommendationProductDTO> searchRecommendations(Object profile, Map<String, String> refParams);

	List<String> searchRecommendation(Map<String, String> refParams, ProductDTO products);

}
