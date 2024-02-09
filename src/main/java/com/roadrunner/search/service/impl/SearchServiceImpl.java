package com.roadrunner.search.service.impl;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.roadrunner.search.constants.ErrorConstants;
import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.ErrorDetailDTO;
import com.roadrunner.search.dto.RelatedProductResponseDTO;
import com.roadrunner.search.dto.response.BaseResponseDTO;
import com.roadrunner.search.helper.BloomreachSearchDTOHelper;
import com.roadrunner.search.service.SearchService;
import com.roadrunner.search.tools.RelatedProductTool;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class SearchServiceImpl implements SearchService {

	@Autowired
	private BloomreachSearchDTOHelper bloomreachSearchDTOHelper;

	@Autowired
	private RelatedProductTool relatedProductTool;

	@Override
	public BaseResponseDTO<BloomreachSearchResultsDTO> restProductSearch(String qUri, HttpServletRequest request) {
		log.debug("SearchServiceImpl::restProductSearch::STARTED qUri={} request={}", qUri, request);
		BaseResponseDTO<BloomreachSearchResultsDTO> response = new BaseResponseDTO<>();
		BloomreachSearchResultsDTO searchResults = null;
		try {
			searchResults = bloomreachSearchDTOHelper.getSearchResults(qUri, request);
			if (searchResults != null) {
				response.setSuccess(Boolean.TRUE);
				response.setState(searchResults);
			} else {
				response.setSuccess(Boolean.FALSE);
				response.getErrors().add(new ErrorDetailDTO(new Date(), ErrorConstants.FETCH_PRODUCTS_ERROR));
			}
		} catch (Exception exception) {
			log.error("SearchServiceImpl::getNewOutletProducts::exception={}", exception);
			response.setSuccess(Boolean.FALSE);
			response.getErrors().add(new ErrorDetailDTO(new Date(), exception.getMessage()));
		}
		log.debug("SearchServiceImpl::restProductSearch::ENDED response={}", response);
		return response;
	}

	@Override
	public BaseResponseDTO<RelatedProductResponseDTO> getRelatedProducts(String productId, String page) {
		log.debug("SearchServiceImpl::getRelatedProducts::STARTED productId={} page={}", productId, page);
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		BaseResponseDTO<RelatedProductResponseDTO> relatedProductResponse = relatedProductTool
				.generateRelatedProducts(productId, page, request);
		log.debug("SearchServiceImpl::getRelatedProducts::ENDED ");
		return relatedProductResponse;
	}

	@Override
	public BaseResponseDTO<RelatedProductResponseDTO> getNewOutletProducts() {
		BaseResponseDTO<RelatedProductResponseDTO> response = new BaseResponseDTO<>();
		try {
			log.debug("SearchServiceImpl::getNewOutletProducts::STARTED");
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
					.getRequest();
			request.setAttribute(SearchConstants.PARAM_OUTLET, Boolean.TRUE);
			RelatedProductResponseDTO relatedProductResponse = null;
			relatedProductResponse = relatedProductTool.fetchNewOutletProducts(null, request);
			if (null == relatedProductResponse) {
				response.setSuccess(Boolean.FALSE);
				response.getErrors().add(new ErrorDetailDTO(new Date(), ErrorConstants.FETCH_NEW_OUTLET_PRODUCT_ERROR));
				return response;
			} else {
				response.setState(relatedProductResponse);
				response.setSuccess(Boolean.TRUE);
			}
		} catch (Exception exception) {
			log.error("SearchServiceImpl::getNewOutletProducts::exception={}", exception);
			response.setSuccess(Boolean.FALSE);
			response.getErrors().add(new ErrorDetailDTO(new Date(), exception.getMessage()));
		}
		log.debug("SearchServiceImpl::getNewOutletProducts::response ={}", response);
		log.debug("SearchServiceImpl::getNewOutletProducts::ENDED ");
		return response;
	}
}
