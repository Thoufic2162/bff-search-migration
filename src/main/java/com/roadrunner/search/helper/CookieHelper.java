package com.roadrunner.search.helper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.RecommendationProductDTO;

import jakarta.servlet.http.HttpServletRequest;

@Component
public interface CookieHelper {

	void addCookies(HttpServletRequest request, List<RecommendationProductDTO> searchProductList,
			BloomreachSearchResultsDTO responseBean);

}
