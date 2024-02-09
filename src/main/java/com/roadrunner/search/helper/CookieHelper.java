package com.roadrunner.search.helper;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.RecommendationProductDTO;

@Component
public interface CookieHelper {

	void addCookies(HttpServletRequest request, List<RecommendationProductDTO> searchProductList,
			BloomreachSearchResultsDTO responseBean);

	void hasHokaPage(boolean isHoka);

	void nonVipExceptHokaCookie(boolean isExceptHoka, HttpServletRequest request);

	void visitAsGuestCookie(boolean hoka, boolean ftv, boolean reset, HttpServletRequest request);

	void nonVipHokaCookie(boolean hoka, boolean ftv, boolean reset, HttpServletRequest request);

}
