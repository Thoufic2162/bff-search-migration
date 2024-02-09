package com.roadrunner.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "rrconfig")
public class RRConfiguration {
	public boolean enableRankingInGenderUrl;
	public boolean enableBloomreachExcluded;
	public boolean enableGenderWithFeedChange;
	public boolean enableProductRanking;
	private boolean enableRunningShoesInWalkingCategory;
	public boolean enableSockInTopPicks;
	public String topPickSockProduct;
	public boolean enableWidenImage;
	public boolean enableAltImagesInPlp;
	public boolean enableSearchToCategoryUrl;
	public boolean enablePLPBanner;
	public boolean enableBloomreachSearch;
	public boolean enablePathwayRecommendation;
	public String RRSDefaultPriceListId;
	public String RRSSalePriceListId;
}
