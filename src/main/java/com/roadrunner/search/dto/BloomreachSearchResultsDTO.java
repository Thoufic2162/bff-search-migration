package com.roadrunner.search.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.roadrunner.search.constants.SearchConstants;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BloomreachSearchResultsDTO {

	private String pageTitle;

	private long totalSearchCount;

	private BrandCategoriesResponse brand;

	private String redirectUrl;

	private String resultQuery;

	private String seoFooterText;

	private List<BRSearchBaseDTO> breadcrums;

	private Map<String, List<BloomreachSearchRefinementsDTO>> refinements;

	private List<RecommendationProductDTO> results;

	private List<BRSearchBaseDTO> sorting;

	private String canonicalUrl;

	private String title;

	private String headerContent;

	private String description;

	private List<String> banners;

	private boolean isRetailUser;

	private String pageUrl;

	private String clearRefUrl;

	private boolean enableDynamicUrl;

	private String h2;

	private String h3;

	private String h1;

	private String keywords;

	private List<BRSearchBaseDTO> customUrl;

	private String seoFooterTextFaq;

	private String seoRedirectUrl;

	private String status = SearchConstants.ACTIVE;

	private boolean displayBrandLandingPage;

	private boolean enableFitProfile;

	private List<Map<String, String>> customerFitProfiles;

	private String brandName;

	private boolean isBrandSearch;

	private boolean enabledisplayFTVmodal;

	private String searchPageBanner;

	private BRMetadata metaData;

	private String searchRedirectURL;

	private boolean isApparel;
}
