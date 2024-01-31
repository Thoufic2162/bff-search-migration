package com.roadrunner.search.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.RelatedProductResponseDTO;
import com.roadrunner.search.service.SearchService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
public class SearchController {

	@Autowired
	private SearchService searchService;

	@GetMapping(value = "/v1/rest-product-search")
	private BloomreachSearchResultsDTO restProductSearch(@RequestParam("qUri") String qUri) {
		log.info("SearchController::restProductSearch::STARTED qUri={}", qUri);
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		BloomreachSearchResultsDTO response = searchService.restProductSearch(qUri, request);
		log.info("SearchController::restProductSearch::ENDED");
		return response;
	}

	@GetMapping("/v1/related-products")
	public RelatedProductResponseDTO getRelatedProducts(String productId) {
		log.info("SearchController::getRelatedProducts::START{} productId={}", productId);
		RelatedProductResponseDTO relatedProducts = searchService.getRelatedProducts(productId);
		log.info("SearchController::getRelatedProducts::ENDED");
		return relatedProducts;
	}

	@GetMapping("/v1/getNewOutletProducts")
	public RelatedProductResponseDTO getNewOutletProducts() {
		log.info("SearchController::getNewOutletProducts::START");
		RelatedProductResponseDTO relatedProducts = searchService.getNewOutletProducts();
		log.info("SearchController::getNewOutletProducts::ENDED");
		return relatedProducts;
	}

}
