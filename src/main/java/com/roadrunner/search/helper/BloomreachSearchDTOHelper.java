package com.roadrunner.search.helper;

import org.springframework.stereotype.Component;

import com.roadrunner.search.dto.BloomreachSearchResultsDTO;

import jakarta.servlet.http.HttpServletRequest;

@Component
public interface BloomreachSearchDTOHelper {

	BloomreachSearchResultsDTO getSearchResults(String qUri, HttpServletRequest request);

}
