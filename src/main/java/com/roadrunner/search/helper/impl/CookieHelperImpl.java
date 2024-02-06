package com.roadrunner.search.helper.impl;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.RecommendationProductDTO;
import com.roadrunner.search.helper.CookieHelper;
import com.roadrunner.search.util.URLCoderUtil;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CookieHelperImpl implements CookieHelper {
	private String first_time_visitor_with_hoka;
	private String first_time_visitor_except_hoka;
	private String returning_nonvip_except_hoka;
	private String returning_nonvip_with_hoka;

	@Override
	public void addCookies(HttpServletRequest request, List<RecommendationProductDTO> searchProductList,
			BloomreachSearchResultsDTO responseBean) {
		log.debug("CookieHelperImpl :: addCookies() :: START :: request {} searchProductList {} responseBean {}",
				request, searchProductList, responseBean);
		List<RecommendationProductDTO> products = searchProductList;
		boolean isHoka = products.stream().allMatch(ishoka -> ishoka.getBrand().contains(SearchConstants.HOKA));
		boolean isHokaMatch = products.stream().anyMatch(ishoka -> ishoka.getBrand().contains(SearchConstants.HOKA));
		String qUri = null;
		boolean isUrlHasHoka = false;
		if (request != null) {
			qUri = URLCoderUtil.decode(request.getParameter(SearchConstants.QURI));
			String referenceURL = request.getHeader(SearchConstants.REFERENCE);
			if (qUri == null && referenceURL != null && referenceURL.contains(SearchConstants.HOKA)) {
				isUrlHasHoka = true;
			}
		}
		boolean isHokaFTV = false;
		boolean isBrandPage = false;
		if (null != qUri) {
			isHokaFTV = qUri.contains(SearchConstants.HOKA);
			isBrandPage = qUri.contains(SearchConstants.BRAND_TYPE_AHEAD_URL);
		}
		if (isHokaFTV || (isHokaMatch && !isBrandPage) || isUrlHasHoka) {
			hasHokaPage(true);
			nonVipExceptHokaCookie(false, request);
			responseBean.setEnabledisplayFTVmodal(true);
		} else {
			hasHokaPage(false);
			nonVipExceptHokaCookie(true, request);
			responseBean.setEnabledisplayFTVmodal(false);
		}
		if (isHoka) {
			visitAsGuestCookie(true, false, true, request);
			nonVipHokaCookie(true, false, true, request);
		} else {
			visitAsGuestCookie(false, false, true, request);
			nonVipHokaCookie(false, false, true, request);
		}
		log.debug("CookieHelperImpl :: addCookies() :: END :: responseBean {}", responseBean);
	}

	@Override
	public void hasHokaPage(boolean isHoka) {
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		if (isHoka) {
			Cookie cookie = new Cookie(SearchConstants.HAS_HOKA_PAGE, SearchConstants.TRUE);
			cookie.setPath(SearchConstants.SLASH);
			response.addCookie(cookie);
		} else {
			Cookie cookie = new Cookie(SearchConstants.HAS_HOKA_PAGE, SearchConstants.FALSE);
			cookie.setPath(SearchConstants.SLASH);
			response.addCookie(cookie);
		}
	}

	@Override
	public void nonVipExceptHokaCookie(boolean isExceptHoka, HttpServletRequest request) {
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		boolean isMember = false;
		String membershipLevel = request.getHeader(SearchConstants.MEMBERSHIPLEVEL);
		if (!StringUtils.isEmpty(membershipLevel)
				&& (SearchConstants.VIP2.equals(membershipLevel) || SearchConstants.VIP3.equals(membershipLevel))) {
			isMember = true;
		}
		if (!isMember && isExceptHoka) {
			Cookie nonVipCookie = new Cookie(SearchConstants.NON_VIP_EXCEPT_HOKA, SearchConstants.TRUE);
			nonVipCookie.setPath(SearchConstants.SLASH);
			response.addCookie(nonVipCookie);
		} else {
			Cookie nonVipCookie = new Cookie(SearchConstants.NON_VIP_EXCEPT_HOKA, SearchConstants.FALSE);
			nonVipCookie.setPath(SearchConstants.SLASH);
			response.addCookie(nonVipCookie);
		}
	}

	public void visitAsGuestCookie(boolean hoka, boolean ftv, boolean reset, HttpServletRequest request) {
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		boolean isMember = false;
		String membershipLevel = request.getHeader(SearchConstants.MEMBERSHIPLEVEL);
		if (!StringUtils.isEmpty(membershipLevel)
				&& (SearchConstants.VIP2.equals(membershipLevel) || SearchConstants.VIP3.equals(membershipLevel))) {
			isMember = true;
		}
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equalsIgnoreCase(SearchConstants.FIRST_TIME_VISITOR_WITH_HOKA))
					first_time_visitor_with_hoka = cookie.getValue();
				if (cookie.getName().equalsIgnoreCase(SearchConstants.FIRST_TIME_VISITOR_EXCEPT_HOKA))
					first_time_visitor_except_hoka = cookie.getValue();
			}
		}
		if (!isMember && ((null == first_time_visitor_with_hoka || null == first_time_visitor_except_hoka) && reset)) {
			if (hoka) {
				Cookie withHokaCookie = new Cookie(SearchConstants.FIRST_TIME_VISITOR_WITH_HOKA, SearchConstants.TRUE);
				withHokaCookie.setPath(SearchConstants.SLASH);
				response.addCookie(withHokaCookie);
				Cookie ExceptHokacookie = new Cookie(SearchConstants.FIRST_TIME_VISITOR_EXCEPT_HOKA,
						SearchConstants.FALSE);
				ExceptHokacookie.setPath(SearchConstants.SLASH);
				response.addCookie(ExceptHokacookie);
			} else {
				Cookie withHokaCookie = new Cookie(SearchConstants.FIRST_TIME_VISITOR_WITH_HOKA, SearchConstants.FALSE);
				withHokaCookie.setPath(SearchConstants.SLASH);
				response.addCookie(withHokaCookie);
				Cookie ExceptHokacookie = new Cookie(SearchConstants.FIRST_TIME_VISITOR_EXCEPT_HOKA,
						SearchConstants.TRUE);
				ExceptHokacookie.setPath(SearchConstants.SLASH);
				response.addCookie(ExceptHokacookie);
			}

		} else {
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equalsIgnoreCase(SearchConstants.RETURNING_NONVIP_WITH_HOKA))
						returning_nonvip_with_hoka = cookie.getValue();
					if (cookie.getName().equalsIgnoreCase(SearchConstants.RETURNING_NONVIP_EXCEPT_HOKA))
						returning_nonvip_except_hoka = cookie.getValue();
				}
			}
			if (!ftv || (ftv && (null == returning_nonvip_with_hoka || null == returning_nonvip_except_hoka))) {
				Cookie withHokaCookie = new Cookie(SearchConstants.FIRST_TIME_VISITOR_WITH_HOKA, SearchConstants.FALSE);
				withHokaCookie.setPath(SearchConstants.SLASH);
				response.addCookie(withHokaCookie);
				Cookie ExceptHokacookie = new Cookie(SearchConstants.FIRST_TIME_VISITOR_EXCEPT_HOKA,
						SearchConstants.FALSE);
				ExceptHokacookie.setPath(SearchConstants.SLASH);
				response.addCookie(ExceptHokacookie);
			}
		}
	}

	public void nonVipHokaCookie(boolean hoka, boolean ftv, boolean reset, HttpServletRequest request) {
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		boolean isMember = false;
		String membershipLevel = request.getHeader(SearchConstants.MEMBERSHIPLEVEL);
		if (!StringUtils.isEmpty(membershipLevel)
				&& (SearchConstants.VIP2.equals(membershipLevel) || SearchConstants.VIP3.equals(membershipLevel))) {
			isMember = true;
		}
		if (!isMember && ((null == returning_nonvip_with_hoka || null == returning_nonvip_except_hoka) && reset)) {
			if (hoka) {
				Cookie withHokaCookie = new Cookie(SearchConstants.RETURNING_NONVIP_WITH_HOKA, SearchConstants.TRUE);
				withHokaCookie.setPath(SearchConstants.SLASH);
				response.addCookie(withHokaCookie);
				Cookie ExceptHokacookie = new Cookie(SearchConstants.RETURNING_NONVIP_EXCEPT_HOKA,
						SearchConstants.FALSE);
				ExceptHokacookie.setPath(SearchConstants.SLASH);
				response.addCookie(ExceptHokacookie);
			} else {
				Cookie withHokaCookie = new Cookie(SearchConstants.RETURNING_NONVIP_WITH_HOKA, SearchConstants.FALSE);
				withHokaCookie.setPath(SearchConstants.SLASH);
				response.addCookie(withHokaCookie);
				Cookie ExceptHokacookie = new Cookie(SearchConstants.RETURNING_NONVIP_EXCEPT_HOKA,
						SearchConstants.TRUE);
				ExceptHokacookie.setPath(SearchConstants.SLASH);
				response.addCookie(ExceptHokacookie);
			}
		} else {
			if (!ftv || (ftv && (null == first_time_visitor_with_hoka || null == first_time_visitor_except_hoka))) {
				Cookie withHokaCookie = new Cookie(SearchConstants.RETURNING_NONVIP_WITH_HOKA, SearchConstants.FALSE);
				withHokaCookie.setPath(SearchConstants.SLASH);
				response.addCookie(withHokaCookie);
				Cookie ExceptHokacookie = new Cookie(SearchConstants.RETURNING_NONVIP_EXCEPT_HOKA,
						SearchConstants.FALSE);
				ExceptHokacookie.setPath(SearchConstants.SLASH);
				response.addCookie(ExceptHokacookie);
			}
		}
	}
}
