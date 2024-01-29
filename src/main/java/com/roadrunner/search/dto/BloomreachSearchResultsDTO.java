package com.roadrunner.search.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.roadrunner.search.constants.SearchConstants;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BloomreachSearchResultsDTO {

	String pageTitle;

	long totalSearchCount;

	BrandCategoriesResponse brand;

	String redirectUrl;

	String resultQuery;

	String seoFooterText;

	List<BRSearchBaseDTO> breadcrums;

	Map<String, List<BloomreachSearchRefinementsDTO>> refinements;

	List<RecommendationProductDTO> results;

	List<BRSearchBaseDTO> sorting;

	String canonicalUrl;

	String title;

	String headerContent;

	String description;

	List<String> banners;

	boolean isRetailUser;

	String pageUrl;

	String clearRefUrl;

	boolean enableDynamicUrl;

	String h2;

	String h3;

	String h1;

	String keywords;

	List<BRSearchBaseDTO> customUrl;

	String seoFooterTextFaq;

	String seoRedirectUrl;

	String status = SearchConstants.ACTIVE;

	boolean displayBrandLandingPage;

	boolean enableFitProfile;

	List<Map<String, String>> customerFitProfiles;

	String brandName;

	boolean isBrandSearch;

	boolean enabledisplayFTVmodal;

	String searchPageBanner;

	private BRMetadata metaData;

	private String searchRedirectURL;

	private boolean isApparel;
}
