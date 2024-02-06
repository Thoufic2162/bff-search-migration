package com.roadrunner.search.helper;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.roadrunner.search.dto.BloomreachSearchResultsDTO;

@Component
public interface BloomreachSearchDTOHelper {

	BloomreachSearchResultsDTO getSearchResults(String qUri, HttpServletRequest request);

}
