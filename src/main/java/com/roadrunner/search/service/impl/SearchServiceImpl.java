package com.roadrunner.search.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.RelatedProductResponseDTO;
import com.roadrunner.search.helper.BloomreachSearchDTOHelper;
import com.roadrunner.search.service.SearchService;
import com.roadrunner.search.tools.RelatedProductTool;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class SearchServiceImpl implements SearchService {

	@Autowired
	private BloomreachSearchDTOHelper bloomreachSearchDTOHelper;

	@Autowired
	private RelatedProductTool relatedProductTool;

	@Override
	public BloomreachSearchResultsDTO restProductSearch(String qUri, HttpServletRequest request) {
		log.debug("SearchServiceImpl::restProductSearch::STARTED qUri={} request={}", qUri, request);
		BloomreachSearchResultsDTO searchResults = bloomreachSearchDTOHelper.getSearchResults(qUri, request);
		log.debug("SearchServiceImpl::restProductSearch::ENDED searchResults={}", searchResults);
		return searchResults;
	}

	@Override
	public RelatedProductResponseDTO getRelatedProducts(String productId) {
		log.debug("SearchServiceImpl::getRelatedProducts::STARTED productId={}", productId);
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		RelatedProductResponseDTO relatedProductResponse = relatedProductTool.generateRelatedProducts(productId,
				request);
		log.debug("SearchServiceImpl::getRelatedProducts::ENDED ");
		return relatedProductResponse;
	}

	@Override
	public RelatedProductResponseDTO getNewOutletProducts() {
		log.debug("SearchServiceImpl::getNewOutletProducts::STARTED");
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		request.setAttribute(SearchConstants.PARAM_OUTLET, Boolean.TRUE);
		RelatedProductResponseDTO relatedProductResponse = relatedProductTool.fetchNewOutletProducts(null, request);
		log.debug("SearchServiceImpl::getNewOutletProducts::ENDED ");
		return relatedProductResponse;
	}
}
