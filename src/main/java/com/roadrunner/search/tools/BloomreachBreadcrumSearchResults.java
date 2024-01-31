package com.roadrunner.search.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.domain.SeoContent;
import com.roadrunner.search.dto.BRSearchBaseDTO;
import com.roadrunner.search.dto.BloomreachSearchResponseDTO;
import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.CatalogElementsFinder;
import com.roadrunner.search.util.BloomreachSearchUtil;
import com.roadrunner.search.util.HttpUtil;
import com.roadrunner.search.util.URLCoderUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@ConfigurationProperties(prefix = "bloomreach-breadcrum-searchresults")
@Getter
@Setter
public class BloomreachBreadcrumSearchResults {

	private Map<String, String> titelOrderMap;
	private Map<String, String> urlMap;
	private Map<String, String> canonicalMap;
	private String postfixTitle;
	private Map<String, String> coopBannersMap;
	private List<String> fitFinderBannerList;
	private List<String> coopBannerList;
	private List<String> korsaBannerList;

	@Autowired
	private CatalogElementsFinder catalogElementsFinder;

	@Autowired
	private BloomreachSearchUtil bloomreachSearchUtil;

	@Autowired
	private Gson gson;

	public void getBreadCrumbs(BloomreachSearchResponseDTO searchResults, HttpServletRequest request,
			BloomreachSearchResultsDTO bloomreachSearchResults) {
		log.debug(
				"BloomreachBreadcrumSearchResults :: getBreadCrumbs() START :: searchResults {} request {} bloomreachSearchResults {}",
				searchResults, request, bloomreachSearchResults);

		Properties queryParams = HttpUtil.getRequestAttribute(request);
		String selNavs = null;
		String qUri = URLCoderUtil.decode(request.getParameter(SearchConstants.QURI));
		if (null != queryParams.getProperty(BloomreachConstants.SEARCH_REDIRECT_URL)) {
			qUri = queryParams.getProperty(SearchConstants.URL_QUERY);
		}
		if (null != queryParams.get(BloomreachConstants.QPARAMS.R)) {
			selNavs = queryParams.get(BloomreachConstants.QPARAMS.R).toString();
		}
		Map<String, String> selectedNavigations = bloomreachSearchUtil.selectedNavigation(selNavs, queryParams);
		List<BRSearchBaseDTO> answers = selectedNavigations.entrySet().stream().map(entry -> {
			BRSearchBaseDTO RBSearchBaseDTO = new BRSearchBaseDTO();
			RBSearchBaseDTO.setName(entry.getKey());
			RBSearchBaseDTO.setDimensionName(entry.getValue());
			return RBSearchBaseDTO;
		}).collect(Collectors.toList());
		bloomreachSearchResults.setBreadcrums(answers);
		if (catalogElementsFinder.getBloomreachUrlQueryMap().containsKey(qUri)) {
			populateBreadCrums(bloomreachSearchResults, qUri);
		}
		constructBreadCrums(queryParams, bloomreachSearchResults, qUri, request);
		log.debug("BloomreachBreadcrumSearchResults :: getBreadCrumbs() END :: queryParams {} qUri {} request {}",
				queryParams, qUri, request);
	}

	private void populateBreadCrums(BloomreachSearchResultsDTO bloomreachSearchResults, String qUri) {
		log.debug(
				"BloomreachBreadcrumSearchResults :: populateBreadCrums() START :: bloomreachSearchResults {} qUri {}",
				bloomreachSearchResults, qUri);
		SeoContent seoRepo = bloomreachSearchUtil.getSeoContent(qUri);
		if (seoRepo == null) {
			return;
		} else {
			if (seoRepo.getBreadCrums() != null) {
				String breadcrumString = seoRepo.getBreadCrums();
				setBreadCrumFromSeo(bloomreachSearchResults, breadcrumString);
			}
			if (seoRepo.getPageTitle() != null) {
				bloomreachSearchResults.setTitle((String) seoRepo.getPageTitle());
			}
			if (seoRepo.getH1() != null) {
				bloomreachSearchResults.setPageTitle((String) seoRepo.getH1());
			}
		}
		log.debug("BloomreachBreadcrumSearchResults :: populateBreadCrums() END :: bloomreachSearchResults {}",
				bloomreachSearchResults);
	}

	private void constructBreadCrums(Properties queryParams, BloomreachSearchResultsDTO bloomreachSearchResults,
			String qUri, HttpServletRequest request) {
		log.debug("BloomreachBreadcrumSearchResults :: constructBreadCrums() START :: queryParams={}, qUri={}",
				queryParams, qUri);
		String resultQuery = (String) queryParams.get(BloomreachConstants.QPARAMS.R);
		AtomicInteger urlCount = new AtomicInteger(0);
		List<BRSearchBaseDTO> toRemove = new ArrayList<BRSearchBaseDTO>();
		List<String> titleString = new ArrayList<String>();
		if (!CollectionUtils.isEmpty(bloomreachSearchResults.getBreadcrums())) {
			SeoContent seoRepo = bloomreachSearchUtil.getSeoContent(qUri);
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.DYNAMIC_URL_REFINMENT))
					&& queryParams.get(SearchConstants.DYNAMIC_URL_REFINMENT).equals(SearchConstants.TRUE)) {
				if (seoRepo != null && seoRepo.getBreadCrums() != null) {
					String breadcrumString = (String) seoRepo.getBreadCrums();
					setBreadCrumFromSeo(bloomreachSearchResults, breadcrumString);
				}
			} else {
				bloomreachSearchResults.getBreadcrums()
						.removeIf(breadcrum -> breadcrum.getName().equals(SearchConstants.UNISEX));
				final String finalQRI = qUri;
				bloomreachSearchResults.getBreadcrums().stream().forEach(breadcrum -> {
					String[] urls = finalQRI.split(SearchConstants.SLASH);
					int urlLength = 0;
					if (finalQRI.contains(SearchConstants.SITE_KIDS)) {
						urlLength = urls.length - 3;
					} else {
						urlLength = urls.length - 2;
					}
					if (urlCount.get() + 1 <= urlLength) {
						StringBuilder urlBuilder = new StringBuilder();
						for (int i = 1; i <= urlCount.get() + 2; i++) {
							urlBuilder.append(SearchConstants.SLASH);
							if (finalQRI.contains(SearchConstants.SITE_KIDS)) {
								urlBuilder.append(urls[i + 1]);
							} else {
								urlBuilder.append(urls[i]);
							}
						}
						if (finalQRI.contains(SearchConstants.SITE_KIDS)) {
							breadcrum.setUrl(SearchConstants.CATEGORY_U + urlBuilder.toString());
						} else {
							breadcrum.setUrl(urlBuilder.toString());
						}
						urlCount.getAndIncrement();
						if (breadcrum.getDimensionName() != null && titelOrderMap.containsKey(breadcrum
								.getDimensionName().replace(SearchConstants.DOT, BloomreachConstants.EMPTY_STRING))) {
							int titleOrder = Integer.parseInt((String) titelOrderMap.get(breadcrum.getDimensionName()
									.replace(SearchConstants.DOT, BloomreachConstants.EMPTY_STRING)).trim());
							breadcrum.setTextIndex(titleOrder);
						}
					} else {
						toRemove.add(breadcrum);
					}
				});
				bloomreachSearchResults.getBreadcrums().removeAll(toRemove);
				bloomreachSearchResults.getBreadcrums().stream().filter(br -> null != br.getName()).forEach(bt -> {
					int index = StringUtils.ordinalIndexOf(finalQRI, SearchConstants.SLASH, 3);
					String prefixUrl = index > 0 ? finalQRI.substring(0, index) : finalQRI;
					if (null != bt.getDimensionName()
							&& bt.getDimensionName().equals(SearchConstants.ENDANGERED.toLowerCase())) {
						bt.setName(SearchConstants.LAST_CHANCE_NAME);
					} else if (null != bt.getDimensionName()
							&& bt.getDimensionName().equals(SearchConstants.RACING.toLowerCase())) {
						bt.setName(SearchConstants.RACING);
					} else if (null != prefixUrl && (urlMap.containsKey(prefixUrl))
							&& bt.getName().equalsIgnoreCase(SearchConstants.PGC_CODE_VALUE)) {
						bt.setName(urlMap.get(prefixUrl));
					}
				});
				if (!CollectionUtils.isEmpty(bloomreachSearchResults.getBreadcrums())) {
					bloomreachSearchResults.getBreadcrums().get(bloomreachSearchResults.getBreadcrums().size() - 1)
							.setUrl(null);
				}
				List<BRSearchBaseDTO> titleList = new ArrayList<BRSearchBaseDTO>(
						bloomreachSearchResults.getBreadcrums());
				Collections.sort(titleList, new SeoRefinementsBeanComparator());
				titleList.forEach(title -> {
					titleString.add(title.getName());
				});
				String title = BloomreachConstants.EMPTY_STRING;
				title = String.join(SearchConstants.SPACE, titleString);
				if (title.contains(BloomreachConstants.OUTLET_STRING)) {
					title = title.replace(BloomreachConstants.OUTLET_STRING, SearchConstants.OUTLET);
				}
				bloomreachSearchResults.setPageTitle(title);
				bloomreachSearchResults.setTitle(title + postfixTitle);
			}
			if (seoRepo != null && seoRepo.getBannerContent() != null) {
				String bannerContent = (String) seoRepo.getBannerContent();
				List<String> breadcrums = null;
				breadcrums = getBreadcrumList(bannerContent, breadcrums);
				if (null != breadcrums && !breadcrums.contains(SearchConstants.GIFT_CARD_BANNER)) {
					if (breadcrums.size() > 1) {
						breadcrums.add(1, SearchConstants.GIFT_CARD_BANNER);
					} else {
						breadcrums.add(SearchConstants.GIFT_CARD_BANNER);
					}
				}
				bloomreachSearchResults.setBanners(breadcrums);
			} else {
				bloomreachSearchResults.setBanners(getBannersList(qUri, resultQuery));
			}
			if (seoRepo != null && seoRepo.getFooterContent() != null) {
				bloomreachSearchResults.setSeoFooterText((String) seoRepo.getFooterContent());
			}
			if (seoRepo != null && seoRepo.getCanonicalUrl() != null) {
				String canonicalUrl = (String) seoRepo.getCanonicalUrl();
				if (null != canonicalUrl) {
					for (String entryKey : canonicalMap.keySet()) {
						if (canonicalUrl.contains(entryKey) && !canonicalUrl.contains(SearchConstants.EQUIPMENT)) {
							canonicalUrl = canonicalUrl.replace(entryKey, canonicalMap.get(entryKey));
						}
					}
				}
				bloomreachSearchResults.setCanonicalUrl(canonicalUrl.toLowerCase());
			} else {
				if (null != qUri) {
					String finalurl = SearchConstants.EMPTY_STRING;
					String canonical = SearchConstants.EMPTY_STRING;
					String words[] = qUri.split(SearchConstants.SLASH);
					String canonicalResult = qUri;
					if ((qUri.contains(SearchConstants.SITE_KIDS) && words.length == 8)
							|| (!qUri.contains(SearchConstants.SITE_KIDS) && words.length == 9)) {
						for (int i = 0; i < words.length - 2; i++) {
							finalurl = finalurl + words[i] + SearchConstants.SLASH;

						}
						StringBuffer removeIndex = new StringBuffer(finalurl);
						canonical = removeIndex.deleteCharAt(finalurl.length() - 1).toString();
						String canonicalUrl = (SearchConstants.ROADRUNNERSPORTS) + canonical;
						if (canonicalUrl != null) {
							bloomreachSearchResults.setCanonicalUrl(canonicalUrl
									.replace(SearchConstants.APOSTROPHE, SearchConstants.EMPTY_STRING).toLowerCase());
						}
					} else {
						if (null != canonicalResult) {
							for (String entryKey : canonicalMap.keySet()) {
								if (canonicalResult.contains(entryKey)
										&& !canonicalResult.contains(SearchConstants.EQUIPMENT)) {
									canonicalResult = canonicalResult.replace(entryKey, canonicalMap.get(entryKey));
								}
							}
						}
						String canonicalUrl = (SearchConstants.ROADRUNNERSPORTS).concat(canonicalResult);
						if (canonicalUrl != null && !canonicalUrl.endsWith(SearchConstants.SLASH)) {
							bloomreachSearchResults.setCanonicalUrl(canonicalUrl
									.replace(SearchConstants.APOSTROPHE, SearchConstants.EMPTY_STRING).toLowerCase());
						} else {
							canonicalUrl = canonicalUrl.substring(0, canonicalUrl.length() - 1);
							bloomreachSearchResults.setCanonicalUrl(canonicalUrl
									.replace(SearchConstants.APOSTROPHE, SearchConstants.EMPTY_STRING).toLowerCase());
						}
					}
				}
			}
			setSeoData(bloomreachSearchResults, seoRepo);
			setCustomUrl(bloomreachSearchResults, seoRepo);
			if (bloomreachSearchResults.getClearRefUrl() == null) {
				bloomreachSearchResults.setClearRefUrl(getClearRefUrl(qUri, request));
			}
		}
		log.debug("BloomreachBreadcrumSearchResults :: constructBreadCrums() END :: bloomreachSearchResults {}",
				bloomreachSearchResults);

	}

	private void setCustomUrl(BloomreachSearchResultsDTO bloomreachSearchResults, SeoContent seoRepo) {
		if (seoRepo != null && seoRepo.getCustomUrl() != null) {
			String customString = (String) seoRepo.getCustomUrl();
			try {
				List<BRSearchBaseDTO> customUrl = gson.fromJson(customString, new TypeToken<List<BRSearchBaseDTO>>() {
				}.getType());
				bloomreachSearchResults.setCustomUrl(customUrl);
			} catch (Exception exception) {
				log.error(
						"BloomreachBreadcrumSearchResults constructBreadCrums() ServletException while getting the bloomreach Results",
						exception);
			}
		}
	}

	private void setSeoData(BloomreachSearchResultsDTO bloomreachSearchResults, SeoContent seoRepo) {
		if (seoRepo != null && seoRepo.getDescription() != null) {
			bloomreachSearchResults.setDescription((String) seoRepo.getDescription());
		}
		if (seoRepo != null && seoRepo.getKeywords() != null) {
			bloomreachSearchResults.setKeywords((String) seoRepo.getKeywords());
		}

		if (seoRepo != null && seoRepo.getH2() != null) {
			bloomreachSearchResults.setH2((String) seoRepo.getH2());
		}
		if (seoRepo != null && seoRepo.getH3() != null) {
			bloomreachSearchResults.setH3((String) seoRepo.getH3());
		}
		if (seoRepo != null && seoRepo.getHeaderContent() != null) {
			bloomreachSearchResults.setHeaderContent((String) seoRepo.getHeaderContent());
		}
		if (seoRepo != null && seoRepo.getPageTitle() != null) {
			bloomreachSearchResults.setTitle((String) seoRepo.getPageTitle());
		}
		if (seoRepo != null && seoRepo.getH1() != null) {
			bloomreachSearchResults.setPageTitle((String) seoRepo.getH1());
		}

		if (seoRepo != null && seoRepo.getRedirectUrl() != null) {
			bloomreachSearchResults.setSeoRedirectUrl((String) seoRepo.getRedirectUrl());
		}

		if (seoRepo != null && seoRepo.getFooterContentFaq() != null) {
			bloomreachSearchResults.setSeoFooterTextFaq((String) seoRepo.getFooterContentFaq());
		}
	}

	private List<String> getBreadcrumList(String bannerContent, List<String> breadcrums) {
		try {

			if (gson.fromJson(bannerContent, JsonElement.class).isJsonArray()) {
				JsonArray jsonArray = gson.fromJson(bannerContent, JsonArray.class);
				String bannerContentString = jsonArray.toString();
				breadcrums = gson.fromJson(bannerContentString, new TypeToken<List<String>>() {
				}.getType());
			}
		} catch (Exception exception) {
			log.error(
					"BloomreachBreadcrumSearchResults constructBreadCrums() ServletException while getting the bloomreach Results exception={}",
					exception);
		}
		return breadcrums;
	}

	private void setBreadCrumFromSeo(BloomreachSearchResultsDTO bloomreachSearchResults, String breadcrumString) {
		try {
			List<BRSearchBaseDTO> breadcrums = gson.fromJson(breadcrumString, new TypeToken<List<BRSearchBaseDTO>>() {
			}.getType());
			bloomreachSearchResults.setBreadcrums(breadcrums);
		} catch (Exception exception) {
			log.error(
					"BloomreachBreadcrumSearchResults setBreadCrumFromSeo() ServletException while getting the bloomreach Results exception={}",
					exception);
		}
	}

	private class SeoRefinementsBeanComparator implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			if (o1 == o2) {
				return 0;
			}
			if (!(o1 instanceof BRSearchBaseDTO) || !(o2 instanceof BRSearchBaseDTO)) {
				return 0;
			}
			BRSearchBaseDTO rb1 = (BRSearchBaseDTO) o1;
			BRSearchBaseDTO rb2 = (BRSearchBaseDTO) o2;
			Integer value1 = rb1.getTextIndex();
			Integer value2 = rb2.getTextIndex();
			if (value1 == null || value2 == null) {
				return 0;
			}
			return value1.compareTo(value2);
		}
	}

	private String getClearRefUrl(String qUri, HttpServletRequest request) {
		log.debug("BloomreachBreadcrumSearchResults :: getClearRefUrl() START :: qUri={}", qUri);
		int refinementActiveCount = 0;

		if (qUri != null && qUri.contains(SearchConstants.SEARCH_CONTEXT_PATH)) {
			return null;
		}

		String[] urlStrings = Optional.ofNullable(qUri).filter(s -> s != null && !s.trim().isEmpty())
				.orElse(BloomreachConstants.EMPTY_STRING).split(SearchConstants.SLASH);

		String activeCount = request.getParameter(SearchConstants.REFINMENT_ACTIVE_COUNT);
		if (activeCount != null) {
			refinementActiveCount = Integer.parseInt(activeCount);
		}
		String url;
		if (urlStrings.length <= 3) {
			url = qUri;
		} else {
			url = IntStream.range(0, urlStrings.length - refinementActiveCount)
					.mapToObj(i -> urlStrings[i] + SearchConstants.SLASH).collect(Collectors.joining());
		}
		log.debug("BloomreachBreadcrumSearchResults :: clearRefUrl{}", url);
		log.debug("BloomreachBreadcrumSearchResults :: getClearRefUrl() END ::");
		if (url != null && url.endsWith(SearchConstants.SLASH)) {
			StringBuffer removeIndex = new StringBuffer(url);
			url = removeIndex.deleteCharAt(url.length() - 1).toString();
		}
		return url;
	}

	private List<String> getBannersList(String canonicalUrl, String resultQuery) {
		log.debug("BloomreachBreadcrumSearchResults :: getBannersList::START canonicalUrl: {}:: resultQuery::{}",
				canonicalUrl, resultQuery);
		List<String> banners = new LinkedList<String>();
		if (canonicalUrl.isEmpty()) {
			return banners;
		}
		if (!canonicalUrl.contains(SearchConstants.CATEGORY_URL)) {
			return banners;
		}
		String brandUrl = canonicalUrl.replaceFirst(SearchConstants.CATEGORY_U, BloomreachConstants.EMPTY_STRING);
		if (brandUrl != null && !brandUrl.isEmpty()) {
			if (fitFinderBannerList != null && !fitFinderBannerList.isEmpty()
					&& fitFinderBannerList.contains(brandUrl)) {
				banners.add(SearchConstants.FIT_FINDER_BANNER);
			}
			if (!CollectionUtils.isEmpty(korsaBannerList) && korsaBannerList.contains(brandUrl)) {
				if (canonicalUrl.contains(SearchConstants.WOMENS)) {
					banners.add(SearchConstants.WOMENS_KORSA_BANNER);
				} else {
					banners.add(SearchConstants.MENS_KORSA_BANNER);
				}
			}
			if (!CollectionUtils.isEmpty(coopBannerList) && coopBannerList.contains(brandUrl)) {
				StringBuffer coop = new StringBuffer(BloomreachConstants.EMPTY_STRING);
				if (brandUrl.contains(SearchConstants.WOMENS)) {
					coop.append(SearchConstants.WOMENS);
					coop.append(SearchConstants.HYPHEN_STRING);
				} else {
					coop.append(SearchConstants.MENS);
					coop.append(SearchConstants.HYPHEN_STRING);
				}
				coop.append(SearchConstants.COOP_BANNER);
				if (coopBannersMap != null && !coopBannersMap.isEmpty() && coopBannersMap.containsKey(brandUrl)) {
					coop.append(SearchConstants.HYPHEN_STRING);
					coop.append(coopBannersMap.get(brandUrl));
				}
				if (!((brandUrl.contains(SearchConstants.WOMENS) && resultQuery != null
						&& resultQuery.contains(SearchConstants.R_MEN))
						|| ((brandUrl.equals(SearchConstants.MEN_RUNNING) || brandUrl.equals(SearchConstants.MEN_TRIAL))
								&& resultQuery != null && resultQuery.contains(SearchConstants.R_WOMEN))
						|| (resultQuery != null && resultQuery.contains(SearchConstants.R_OUTLET))
						|| ((brandUrl.equals(SearchConstants.MEN_RUNNING)
								|| brandUrl.equals(SearchConstants.WOMEN_RUNNING))
								&& resultQuery != null
								&& (resultQuery.contains(SearchConstants.R_TRAIL_RUNNING)
										|| resultQuery.contains(SearchConstants.R_P_TRAIL_RUNNING)))
						|| ((brandUrl.equals(SearchConstants.WOMEN_TRIAL) || brandUrl.equals(SearchConstants.MEN_TRIAL))
								&& resultQuery != null && (resultQuery.contains(SearchConstants.R_RUNNING)
										|| resultQuery.contains(SearchConstants.R_P_RUNNING))))) {
					banners.add(coop.toString());
				}
			}
		}
		if (banners.size() > 1) {
			banners.add(1, SearchConstants.GIFT_CARD_BANNER);
		} else {
			banners.add(SearchConstants.GIFT_CARD_BANNER);
		}
		log.debug("BloomreachBreadcrumSearchResults:: getBannersList:: END:: banners::{}", banners);
		return banners;
	}

}
