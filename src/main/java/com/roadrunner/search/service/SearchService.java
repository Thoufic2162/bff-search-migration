package com.roadrunner.search.service;

import org.springframework.stereotype.Service;

import com.roadrunner.search.dto.BloomreachSearchResultsDTO;

import jakarta.servlet.http.HttpServletRequest;

@Service
public interface SearchService {

	BloomreachSearchResultsDTO restProductSearch(String qUri, HttpServletRequest request);

}
