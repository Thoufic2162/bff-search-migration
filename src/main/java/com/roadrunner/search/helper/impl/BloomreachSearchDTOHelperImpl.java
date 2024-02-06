package com.roadrunner.search.helper.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.roadrunner.search.config.RRConfiguration;
import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.domain.SeoContent;
import com.roadrunner.search.dto.BRSearchBaseDTO;
import com.roadrunner.search.dto.BloomreachSearchRefinementsDTO;
import com.roadrunner.search.dto.BloomreachSearchResponseDTO;
import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.BrandCategoriesResponse;
import com.roadrunner.search.dto.CatalogElementsFinder;
import com.roadrunner.search.dto.CategoryItemDTO;
import com.roadrunner.search.dto.RecommendationProductDTO;
import com.roadrunner.search.helper.BloomreachSearchDTOHelper;
import com.roadrunner.search.helper.SearchHelper;
import com.roadrunner.search.tools.BloomreachBreadcrumSearchResults;
import com.roadrunner.search.tools.BloomreachProductSearchResults;
import com.roadrunner.search.tools.BloomreachRefinementSearchResults;
import com.roadrunner.search.tools.BrandCategoryTool;
import com.roadrunner.search.util.BloomreachSearchUtil;
import com.roadrunner.search.util.HttpUtil;
import com.roadrunner.search.util.URLCoderUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@ConfigurationProperties(prefix = "searchhelper")
@SuppressWarnings({ "deprecation", "unused" })
@Getter
@Setter
public class BloomreachSearchDTOHelperImpl implements BloomreachSearchDTOHelper {

	private List<String> brandList;
	private Map<String, String> brandMap;
	private String dropShipOnlyNavigation;
	private String dropShipNavigation;
	private List<String> dropShipQueryList;
	private String sauconyBrandNavigation;
	private String reebokBrandNavigation;
	private String sprintsNavigation;
	private Map<String, String> giftGuideUrlMap;
	private List<String> brandQueryList;
	private String seoTitleText;
	private List<String> giftGuideUrlDisableRefinement;
	private String bannerName;
	private int bannerIndex;
	private Map<String, String> customQueryBannersMap;
	private List<String> categoryList;
	private List<String> skipCategoryList;
	private Map<String, String> sortOptions;
	private List<String> refinementSortList;

	@Value("${bloomreachrefinement.skipClpUrl}")
	private List<String> skipClpUrl;
	@Value("${bloomreachsearchutil.genderUrlList}")
	private List<String> genderUrlList;

	@Autowired
	private RRConfiguration rrConfiguration;
	@Autowired
	private BloomreachSearchUtil bloomreachSearchUtil;

	@Autowired
	private SearchHelper searchHelper;

	@Autowired
	private CatalogElementsFinder catalogElementsFinder;

	@Autowired
	private BloomreachProductSearchResults bloomreachProductSearchResults;

	@Autowired
	private BloomreachRefinementSearchResults bloomreachRefinementSearchResults;

	@Autowired
	private BloomreachBreadcrumSearchResults bloomreachBreadcrumSearchResults;

	@Autowired
	private BrandCategoryTool brandCategoryTool;

	@Override
	public BloomreachSearchResultsDTO getSearchResults(String qUri, HttpServletRequest request) {
		log.debug("BloomreachSearchDTOHelperImpl::getSearchResults()::STARTED");
		BloomreachSearchResultsDTO bloomreachResults = null;
		BloomreachSearchResponseDTO searchResults = null;
		bloomreachSearchUtil.constructQueryParams(request);
		Properties queryParams = HttpUtil.getRequestAttributesAndParameters(request);
		String isMyPerfectFit = request.getParameter(SearchConstants.IS_PERFECT_FIT);
		qUri = URLCoderUtil.decode(request.getParameter(SearchConstants.QURI));
		if (null != queryParams.getProperty(BloomreachConstants.SEARCH_REDIRECT_URL)) {
			qUri = queryParams.getProperty(SearchConstants.URL_QUERY);
		}
		if (null != qUri) {
			qUri = qUri.replace(SearchConstants.APOSTROPHE, SearchConstants.EMPTY_STRING).toLowerCase();
		}
		String skip = null;
		String sort = null;
		String page = null;
		String dropship = null;

		if (!StringUtils.isEmpty(queryParams.getProperty(BloomreachConstants.QPARAMS.P))) {
			page = queryParams.getProperty(BloomreachConstants.QPARAMS.P);
		}
		if (!StringUtils.isEmpty(queryParams.getProperty(BloomreachConstants.QPARAMS.SKIP))) {
			skip = queryParams.getProperty(BloomreachConstants.QPARAMS.SKIP);
		}
		if (!StringUtils.isEmpty(queryParams.getProperty(BloomreachConstants.QPARAMS.S))) {
			sort = queryParams.getProperty(BloomreachConstants.QPARAMS.S);
		}
		if (sort == null) {
			queryParams.setProperty(BloomreachConstants.QPARAMS.S, BloomreachConstants.START_COUNT);
		}
		String seoRootString = (String) queryParams.get(SearchConstants.SEOROOT);
		log.debug("BloomreachSearchDTOHelperImpl getSearchResults() queryParams {}", queryParams);
		String skipSearchStr = queryParams.getProperty(SearchConstants.SKIP_SEARCH);
		boolean skipSearch = SearchConstants.TRUE.equalsIgnoreCase(skipSearchStr);
		log.debug("BloomreachSearchDTOHelperImpl.getSearchResults(): skipSearchStr=" + skipSearchStr + ", skipSearch="
				+ skipSearch);
		if (!skipSearch) {
			if (qUri == null || qUri.contains(SearchConstants.SEARCH_CONTEXT_PATH)) {
				boolean isSearch = false;
				String query = queryParams.getProperty(BloomreachConstants.QPARAMS.QUERY);
				String brandQuery = query.toLowerCase().replace(SearchConstants.SPACE, BloomreachConstants.HYPHEN);
				bloomreachResults = new BloomreachSearchResultsDTO();
				boolean isContainsBrand = brandList.stream()
						.anyMatch(brandList -> brandList.equalsIgnoreCase(brandQuery));
				if (isContainsBrand) {
					bloomreachResults.setBrandSearch(Boolean.TRUE);
					bloomreachResults.setBrandName(brandQuery);
					if (brandMap.containsKey(brandQuery)) {
						bloomreachResults
								.setRedirectUrl(SearchConstants.BRAND_TYPE_AHEAD_URL + brandMap.get(brandQuery));
					} else {
						bloomreachResults.setRedirectUrl(SearchConstants.BRAND_TYPE_AHEAD_URL.concat(brandQuery));
					}
					return bloomreachResults;
				}
				String rQueryParam = queryParams.getProperty(BloomreachConstants.QPARAMS.R);
				if (dropShipQueryList.contains(query.toLowerCase())) {
					queryParams.setProperty(BloomreachConstants.QPARAMS.QUERY, SearchConstants.EMPTY_STRING);
					if ((query.toLowerCase()).equalsIgnoreCase(BloomreachConstants.DROPSHIP_QUERY)) {
						if (null != rQueryParam) {
							queryParams.setProperty(BloomreachConstants.QPARAMS.R,
									queryParams.getProperty(BloomreachConstants.QPARAMS.R) + SearchConstants.REGEX_COMMA
											+ dropShipNavigation);
							dropship = SearchConstants.REGEX_COMMA + dropShipNavigation;
						} else {
							queryParams.setProperty(BloomreachConstants.QPARAMS.R, dropShipNavigation);
							dropship = dropShipNavigation;
						}
					}
					if ((query.toLowerCase()).contains(SearchConstants.DROP_SHIP_ONLY)) {
						if (null != rQueryParam) {
							queryParams.setProperty(BloomreachConstants.QPARAMS.R,
									queryParams.getProperty(BloomreachConstants.QPARAMS.R) + SearchConstants.REGEX_COMMA
											+ dropShipNavigation + SearchConstants.REGEX_COMMA
											+ dropShipOnlyNavigation);
							dropship = SearchConstants.REGEX_COMMA + dropShipNavigation + SearchConstants.REGEX_COMMA
									+ dropShipOnlyNavigation;
						} else {
							queryParams.setProperty(BloomreachConstants.QPARAMS.R,
									dropShipNavigation + SearchConstants.REGEX_COMMA + dropShipOnlyNavigation);
							dropship = dropShipNavigation + SearchConstants.REGEX_COMMA + dropShipOnlyNavigation;
						}
					}
				}
				if (query.toLowerCase().contains(SearchConstants.RIDE)
						&& queryParams.getProperty(BloomreachConstants.QPARAMS.SZ) == null
						&& !query.toLowerCase().contains(SearchConstants.WAVE_RIDER)) {
					if (query.toLowerCase().contains(SearchConstants.FLOATRIDE)) {
						queryParams.setProperty(BloomreachConstants.QPARAMS.QUERY, SearchConstants.FLOATRIDE);
						queryParams.setProperty(BloomreachConstants.QPARAMS.R,
								queryParams.getProperty(BloomreachConstants.QPARAMS.R) + SearchConstants.REGEX_COMMA
										+ reebokBrandNavigation);
					} else {
						queryParams.setProperty(BloomreachConstants.QPARAMS.QUERY, SearchConstants.RIDE);
						queryParams.setProperty(BloomreachConstants.QPARAMS.R,
								queryParams.getProperty(BloomreachConstants.QPARAMS.R) + SearchConstants.REGEX_COMMA
										+ sauconyBrandNavigation);
					}
				}

				if (query.toLowerCase().contains(SearchConstants.SALE)) {
					queryParams.setProperty(BloomreachConstants.QPARAMS.QUERY, BloomreachConstants.EMPTY_STRING);
					queryParams.setProperty(BloomreachConstants.QPARAMS.R, BloomreachConstants.RRS_SALE
							.concat(SearchConstants.COLON).concat(BloomreachConstants.ON_SALE));
				}

				if (query.toLowerCase().contains(SearchConstants.GUIDE)
						&& queryParams.getProperty(BloomreachConstants.QPARAMS.SZ) == null) {
					queryParams.setProperty(BloomreachConstants.QPARAMS.QUERY, SearchConstants.GUIDE);
					queryParams.setProperty(BloomreachConstants.QPARAMS.R,
							queryParams.getProperty(BloomreachConstants.QPARAMS.R) + SearchConstants.REGEX_COMMA
									+ sauconyBrandNavigation);
				}
				if (query.toLowerCase().contains(SearchConstants.KINVARA)
						&& queryParams.getProperty(BloomreachConstants.QPARAMS.SZ) == null) {
					queryParams.setProperty(BloomreachConstants.QPARAMS.QUERY, SearchConstants.KINVARA);
					queryParams.setProperty(BloomreachConstants.QPARAMS.R,
							queryParams.getProperty(BloomreachConstants.QPARAMS.R) + SearchConstants.REGEX_COMMA
									+ sauconyBrandNavigation);
				}
				if (query.toLowerCase().contains(SearchConstants.SPRINTS)
						&& queryParams.getProperty(BloomreachConstants.QPARAMS.SZ) == null) {
					queryParams.setProperty(BloomreachConstants.QPARAMS.R, sprintsNavigation);
				}

				if (rrConfiguration.isEnableRunningShoesInWalkingCategory()
						&& query.equalsIgnoreCase(BloomreachConstants.WALKING_SHOES_STRING)) {
					String rParam = (String) queryParams.get(BloomreachConstants.QPARAMS.R);
					queryParams.setProperty(BloomreachConstants.QPARAMS.QUERY, SearchConstants.SHOE);
					if (null != rParam) {
						queryParams.setProperty(BloomreachConstants.QPARAMS.R, rParam);
					} else {
						queryParams.setProperty(BloomreachConstants.QPARAMS.R,
								BloomreachConstants.CAT_WALKING_AND_RUNNING);
					}
				}
				boolean querySearch = !StringUtils.isEmpty(query);
				if (querySearch) {
					List<CategoryItemDTO> categoryItem = null;
					categoryItem = bloomreachSearchUtil.getCategoryItem(query);
					if (!CollectionUtils.isEmpty(categoryItem)) {
						List<String> refinements = categoryItem.stream().map(CategoryItemDTO::getRefinements).flatMap(
								refinementsList -> Arrays.stream(refinementsList.split(SearchConstants.SEMICOLON)))
								.collect(Collectors.toList());
						String categoryName = categoryItem.get(0).getCategoryName();
						String param = queryParams.getProperty(BloomreachConstants.QPARAMS.R);
						param = null != param ? BloomreachConstants.COMMA.concat(param)
								: BloomreachConstants.EMPTY_STRING;
						if ((!CollectionUtils.isEmpty(refinements) && null != param
								&& !param.contains(BloomreachConstants.DROP_SHIP))
								|| (!CollectionUtils.isEmpty(refinements) && null != categoryName
										&& categoryName.equalsIgnoreCase(BloomreachConstants.SHOE_DOG_SEARCH))) {
							String rParam = String.join(BloomreachConstants.COMMA, refinements);
							queryParams.setProperty(BloomreachConstants.QPARAMS.QUERY,
									BloomreachConstants.EMPTY_STRING);
							queryParams.setProperty(BloomreachConstants.QPARAMS.R, rParam.concat(param));
						}
					}
				}
				if (StringUtils.isNotBlank(queryParams.getProperty(BloomreachConstants.QPARAMS.QUERY))) {
					isSearch = true;
				}
				searchResults = searchHelper.performSearch(queryParams, request, isSearch);
			} else {
				if (qUri.contains(SearchConstants.GIFT_GUIDE_SEARCH_CONTEXT_PATH)) {
					queryParams.setProperty(BloomreachConstants.QPARAMS.QUERY, giftGuideUrlMap.get(qUri));
					queryParams.setProperty(SearchConstants.IS_CUSTOMQUERY, SearchConstants.TRUE);
					if (!giftGuideUrlMap.get(qUri).contains(SearchConstants.KORSA_GIFT_GUIDE)) {
						queryParams.setProperty(BloomreachConstants.QPARAMS.SZ, SearchConstants.TRUE);
					}
					searchResults = searchHelper.performSearch(queryParams, request, true);
				} else if ((qUri != null) && qUri.contains(SearchConstants.BRAND_TYPE_AHEAD_URL)) {
					String rParam = BloomreachConstants.EMPTY_STRING;
					if (null != queryParams.get(BloomreachConstants.QPARAMS.R)) {
						rParam = BloomreachConstants.COMMA + (String) queryParams.get(BloomreachConstants.QPARAMS.R);
					}
					queryParams.setProperty(BloomreachConstants.BRAND_PAGE_NAME,
							queryParams.getProperty(BloomreachConstants.QPARAMS.QUERY));
					if (brandQueryList.contains(queryParams.getProperty(BloomreachConstants.QPARAMS.QUERY))) {
						String brandParam = SearchConstants.BRAND_PARAMETER
								.concat(catalogElementsFinder.getBloomreachBrandMap()
										.get(queryParams.getProperty(BloomreachConstants.QPARAMS.QUERY)));
						if (null != queryParams.getProperty(BloomreachConstants.QPARAMS.R)) {
							queryParams.setProperty(BloomreachConstants.QPARAMS.R,
									queryParams.getProperty(BloomreachConstants.QPARAMS.R)
											.concat(BloomreachConstants.COMMA).concat(brandParam));
						} else {
							queryParams.setProperty(BloomreachConstants.QPARAMS.R, brandParam);
						}
						queryParams.setProperty(BloomreachConstants.QPARAMS.QUERY, BloomreachConstants.EMPTY_STRING);
					}
					searchResults = searchHelper.performSearch(queryParams, request, false);
				} else if ((qUri != null) && qUri.contains(SearchConstants.SEARCH_CONTEXT_PATH)) {
					searchResults = searchHelper.performSearch(queryParams, request, true);
				} else {
					searchResults = searchHelper.performSearch(queryParams, request, false);
				}
				if (null != searchResults && null != searchResults.getResponse()
						&& searchResults.getResponse().getNumFound() == 0) {
					queryParams.setProperty(BloomreachConstants.QPARAMS.QUERY, SearchConstants.STAR);
					queryParams.remove(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR);
					searchResults = searchHelper.performSearch(queryParams, request, true);
				}
				if ((isMyPerfectFit != null && isMyPerfectFit.equalsIgnoreCase(SearchConstants.TRUE))
						&& (searchResults == null || null == searchResults.getResponse()
								|| searchResults.getResponse().getNumFound() == 0)) {
					String r_query = queryParams.getProperty(BloomreachConstants.R_QUERY);
					String updated_r_ = queryParams.getProperty(BloomreachConstants.QPARAMS.R).replaceAll(r_query,
							SearchConstants.EMPTY_STRING);
					String r_value = StringUtils.chop(updated_r_);
					queryParams.setProperty(BloomreachConstants.QPARAMS.R, r_value);
					queryParams.setProperty(BloomreachConstants.R_QUERY, SearchConstants.EMPTY_STRING);
					queryParams.setProperty(BloomreachConstants.SEARCH_REDIRECT_URL, qUri);
					searchResults = searchHelper.performSearch(queryParams, request, false);
					request.setAttribute(SearchConstants.R, r_value);
					request.setAttribute(BloomreachConstants.R_QUERY, SearchConstants.EMPTY_STRING);
				}
			}
		}
		if (rrConfiguration.isEnableRankingInGenderUrl() && genderUrlList.contains(qUri)
				&& null != queryParams.get(BloomreachConstants.IS_GENDER)) {
			request.setAttribute(BloomreachConstants.QPARAMS.R,
					BloomreachConstants.GENDER_TEXT.concat((String) queryParams.get(BloomreachConstants.IS_GENDER)));
		}
		String customQueryurl = queryParams.getProperty(BloomreachConstants.QPARAMS.SZ);
		boolean isZQuery = SearchConstants.TRUE.equalsIgnoreCase(customQueryurl);
		String count = (String) queryParams.get(SearchConstants.QUERY_COUNT);
		String isCustomQuery = (String) queryParams.get(SearchConstants.IS_CUSTOMQUERY);
		bloomreachResults = new BloomreachSearchResultsDTO();
		if (searchResults != null && searchResults.getKeywordRedirect() != null) {
			bloomreachResults.setTotalSearchCount(SearchConstants.NUMBER_ONE);
			bloomreachResults.setRedirectUrl(searchResults.getKeywordRedirect().getRedirectUrl());
			log.debug("BloomreachSearchDTOHelperImpl getSearchResults() keyword redirecting");
			return bloomreachResults;
		}
		if (rrConfiguration.isEnableSearchToCategoryUrl()
				&& null != queryParams.getProperty(BloomreachConstants.SEARCH_REDIRECT_URL)) {
			bloomreachResults.setSearchRedirectURL(qUri);
		}
		if (null != qUri && qUri.length() > 0) {
			String pageUrl = bloomreachSearchUtil.getPageURL(qUri);
			bloomreachResults.setPageUrl(pageUrl);
		}
		if (null != qUri && qUri.contains(SearchConstants.BRAND_TYPE_AHEAD_URL)) {
			populateSeoContentData(qUri, bloomreachResults);
		}
		if (null != qUri && qUri.contains(SearchConstants.BRAND_TYPE_AHEAD_URL)) {
			String qUriString = qUri.toString();
			String separator = SearchConstants.SLASH;
			int indexOfSeparator = qUriString.lastIndexOf(separator);
			String brandname = qUriString.substring(indexOfSeparator + 1);
			brandname = getCatalogElementsFinder().getQueryMap().containsKey(brandname)
					? getCatalogElementsFinder().getQueryMap().get(brandname)
					: Arrays.stream(brandname.split(BloomreachConstants.HYPHEN))
							.map(s -> WordUtils.capitalizeFully(s, BloomreachConstants.SINGLE_QUOTES_SPACE,
									BloomreachConstants.SINGLE_QUOTES_HYPHEN))
							.collect(Collectors.joining(BloomreachConstants.PLUS));
			BrandCategoriesResponse brandCat = brandCategoryTool
					.getBrandData(brandname.replace(SearchConstants.PLUS, SearchConstants.SPACE));
			if (brandCat != null && brandCat.getBrandCategories() != null && !brandCat.getBrandCategories().isEmpty()) {
				bloomreachResults.setBrand(brandCat);
			}
		}
		if (bloomreachResults.getBrand() != null
				&& bloomreachResults.getBrand().getBrandType().equals(BloomreachConstants.LARGE)) {
			return bloomreachResults;
		}
		if (!(giftGuideUrlDisableRefinement.contains(qUri))) {
			bloomreachRefinementSearchResults.getRefinementSearchResults(searchResults, request, bloomreachResults);
		}
		bloomreachProductSearchResults.getProductResults(searchResults, request, bloomreachResults);
		bloomreachBreadcrumSearchResults.getBreadCrumbs(searchResults, request, bloomreachResults);
		if (rrConfiguration.isEnablePLPBanner() && qUri.contains(SearchConstants.CATEGORY_URL)) {
			List<RecommendationProductDTO> result = bloomreachResults.getResults();
			RecommendationProductDTO recommendationProduct = new RecommendationProductDTO();
			recommendationProduct.setBanner(bannerName);
			if (!CollectionUtils.isEmpty(result) && result.size() >= bannerIndex) {
				result.add(bannerIndex, recommendationProduct);
				bloomreachResults.setResults(result);
			}
		}
		String resultQuery = queryParams.getProperty(BloomreachConstants.QPARAMS.R);
		if (searchResults != null && null != searchResults.getResponse()) {
			bloomreachResults.setTotalSearchCount(searchResults.getResponse().getNumFound());
		}
		if (null != qUri && customQueryBannersMap != null && customQueryBannersMap.size() > 0
				&& qUri.contains(SearchConstants.SEARCH_CONTEXT_PATH) && isZQuery) {
			bloomreachResults.setSearchPageBanner(getBannersList(qUri, customQueryBannersMap));
		}
		if (null != qUri && qUri.contains(SearchConstants.SEARCH_CONTEXT_PATH)
				&& null != request.getAttribute(BloomreachConstants.CLEAR_FILTER_URL)) {
			bloomreachResults.setClearRefUrl((String) request.getAttribute(BloomreachConstants.CLEAR_FILTER_URL));
		}
		bloomreachResults.setResultQuery(resultQuery);
		if (count != null && count.equalsIgnoreCase(SearchConstants.TRUE)) {
			bloomreachResults.setStatus(BloomreachConstants.INACTIVE);
		}
		if (isCustomQuery != null && isCustomQuery.equalsIgnoreCase(SearchConstants.FALSE)) {
			bloomreachResults.setStatus(BloomreachConstants.INACTIVE);
		}
		if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.REMOVE_CATAGORY))
				&& queryParams.get(SearchConstants.REMOVE_CATAGORY).equals(SearchConstants.TRUE)
				&& bloomreachResults.getRefinements() != null && !bloomreachResults.getRefinements().isEmpty()) {
			final String uri = qUri;
			Boolean isSkipCategory = skipCategoryList.stream().anyMatch(url -> url.equals(uri));
			Boolean isSkipClp = skipClpUrl.stream().allMatch(genderType -> !uri.contains(genderType));
			if (null != isSkipCategory && isSkipCategory
					|| (uri.contains(SearchConstants.CATEGORY_URL) && isSkipClp != null && isSkipClp)) {
				bloomreachResults.getRefinements().keySet().remove(BloomreachConstants.CATEGORY);
			} else {
				bloomreachResults.getRefinements().keySet().removeAll(categoryList);
			}
		}
		AtomicInteger positionToInsert = new AtomicInteger();
		Map<String, List<BloomreachSearchRefinementsDTO>> refinementsOrder = new LinkedHashMap<String, List<BloomreachSearchRefinementsDTO>>();
		if (!CollectionUtils.isEmpty(bloomreachResults.getRefinements())
				&& bloomreachResults.getRefinements().containsKey(BloomreachConstants.CUSTOMER_RATING)) {
			List<BloomreachSearchRefinementsDTO> rating = bloomreachResults.getRefinements()
					.get(BloomreachConstants.CUSTOMER_RATING);
			bloomreachResults.getRefinements().remove(BloomreachConstants.CUSTOMER_RATING);
			bloomreachResults.getRefinements().entrySet().stream().forEach(entry -> {
				if (positionToInsert.get() == 3) {
					refinementsOrder.put(BloomreachConstants.CUSTOMER_RATING, rating);
				}
				refinementsOrder.put(entry.getKey(), entry.getValue());
				positionToInsert.set(positionToInsert.get() + 1);
			});
			bloomreachResults.setRefinements(refinementsOrder);
		}
		if (!CollectionUtils.isEmpty(bloomreachResults.getRefinements())
				&& !CollectionUtils.isEmpty(bloomreachResults.getRefinements().get(BloomreachConstants.BRAND))) {
			bloomreachResults.getRefinements().get(BloomreachConstants.BRAND).stream()
					.filter(s -> s.getUrl() != null && s.getUrl().equals(SearchConstants.CATEGORY_U)).forEach(s -> {
						s.setUrl(s.getUrl().replace(SearchConstants.CATEGORY_U, SearchConstants.BRAND_TYPE_AHEAD_URL));
					});
		}
		sort = queryParams.getProperty(BloomreachConstants.QPARAMS.S);
		List<BRSearchBaseDTO> sortList = new ArrayList<>();
		for (Map.Entry<String, String> entry : sortOptions.entrySet()) {
			BRSearchBaseDTO BRSearchBase = new BRSearchBaseDTO();
			BRSearchBase.setName(entry.getValue());
			if (null != queryParams.getProperty(BloomreachConstants.QPARAMS.R)) {
				String url = BloomreachConstants.R_EQUAL + queryParams.getProperty(BloomreachConstants.QPARAMS.R);
				if (null != qUri && qUri.contains(SearchConstants.BRAND_TYPE_AHEAD_URL)) {
					String currentBrandName = BloomreachConstants.BRAND_PARAMETER + catalogElementsFinder
							.getBloomreachBrandMap().get(queryParams.getProperty(BloomreachConstants.BRAND_PAGE_NAME));
					url = url.replaceAll(currentBrandName, BloomreachConstants.EMPTY_STRING);
				}
				if (null != queryParams.get(BloomreachConstants.BR_QUERY_PARAM_DYNAMIC_URL_REFINMENT)) {
					String deSelectParam = (String) queryParams
							.get(BloomreachConstants.BR_QUERY_PARAM_DYNAMIC_URL_REFINMENT);
					deSelectParam = URLCoderUtil.decode(deSelectParam);
					url = url.replaceAll(URLCoderUtil.decode(deSelectParam), BloomreachConstants.EMPTY_STRING);
				}
				if (null != dropship) {
					url = url.replace(dropship, BloomreachConstants.EMPTY_STRING);
				}
				url = url.replace(BloomreachConstants.PARAMETER_R_WITH_COMMA, BloomreachConstants.R_EQUAL)
						.replaceAll(SearchConstants.SPACE, BloomreachConstants.PLUS)
						.replaceAll(BloomreachConstants.URL_DELIMETER, BloomreachConstants.PERCENTAGE_26);
				if (null != url && url.equalsIgnoreCase(BloomreachConstants.R_EQUAL)
						&& null != queryParams.get(SearchConstants.URL_QUERY)) {
					url = (String) queryParams.get(SearchConstants.URL_QUERY);
					for (String param : refinementSortList) {
						if (null != queryParams.get(param)) {
							url = url.concat(BloomreachConstants.URL_DELIMETER).concat(
									param.concat(BloomreachConstants.EQUAL).concat((String) queryParams.get(param)));
						}
					}
				}
				BRSearchBase.setUrl(url + BloomreachConstants.URL_DELIMETER + BloomreachConstants.PAGE_ROW
						+ BloomreachConstants.URL_DELIMETER + BloomreachConstants.PARAMETER_S + entry.getKey());
			} else {
				BRSearchBase.setUrl(BloomreachConstants.PAGE_ROW + BloomreachConstants.URL_DELIMETER
						+ BloomreachConstants.PARAMETER_S + entry.getKey());
			}
			if (null != sort && entry.getKey().equals(sort)) {
				BRSearchBase.setState(BloomreachConstants.ACTIVE);
			} else {
				BRSearchBase.setState(BloomreachConstants.INACTIVE);
			}
			sortList.add(BRSearchBase);
		}
		if (bloomreachResults.getResultQuery() != null) {
			String webpgc = bloomreachResults.getResultQuery();
			if (webpgc.contains(BloomreachConstants.WEBPGC) && !webpgc.contains(BloomreachConstants.SHOES)) {
				bloomreachResults.setApparel(Boolean.TRUE);
			}
		}
		bloomreachResults.setSorting(sortList);
		log.debug("BloomreachSearchDTOHelperImpl getSearchResults() END ");
		return bloomreachResults;
	}

	private void populateSeoContentData(String qUri, BloomreachSearchResultsDTO bloomreachResults) {
		String brandname = SearchConstants.EMPTY_STRING;
		SeoContent seoRepo = bloomreachSearchUtil.getSeoContent(qUri);
		String qUriString = qUri.toString();
		String separator = SearchConstants.SLASH;
		int indexOfSeparator = qUriString.lastIndexOf(separator);
		brandname = qUriString.substring(indexOfSeparator + 1);
		String toRemove1 = SearchConstants.HYPHEN_STRING;
		if (brandname.contains(toRemove1)) {
			brandname = brandname.replaceAll(toRemove1, SearchConstants.SPACE);
		}
		brandname = WordUtils.capitalize(brandname);
		String pageTitle = SearchConstants.EMPTY_STRING;
		String seoTitleH1 = SearchConstants.EMPTY_STRING;
		if (seoRepo != null && seoRepo.getPageTitle() != null) {
			log.debug("BloomreachSearchDTOHelperImpl populateSeoContentData(): seoRepo= {} qUri = {} ", seoRepo, qUri);
			pageTitle = (String) seoRepo.getPageTitle();
		}
		if (seoRepo != null && seoRepo.getH1() != null) {
			seoTitleH1 = (String) seoRepo.getH1();
		}
		if (StringUtils.isBlank(seoTitleH1)) {
			bloomreachResults.setPageTitle(brandname);
		} else {
			bloomreachResults.setPageTitle(seoTitleH1);
		}

		if (StringUtils.isBlank(pageTitle)) {
			pageTitle = MessageFormat.format(seoTitleText, brandname);
			bloomreachResults.setTitle(pageTitle);
		} else {
			bloomreachResults.setTitle(pageTitle);
		}
		if (seoRepo != null && seoRepo.getDescription() != null) {
			bloomreachResults.setDescription((String) seoRepo.getDescription());
		}
		if (seoRepo != null && seoRepo.getKeywords() != null) {
			bloomreachResults.setKeywords((String) seoRepo.getKeywords());
		}

		if (seoRepo != null && seoRepo.getH2() != null) {
			bloomreachResults.setH2((String) seoRepo.getH2());
		}
		if (seoRepo != null && seoRepo.getH3() != null) {
			bloomreachResults.setH3((String) seoRepo.getH3());
		}
		if (seoRepo != null && seoRepo.getFooterContentFaq() != null) {
			bloomreachResults.setSeoFooterTextFaq((String) seoRepo.getFooterContentFaq());
		}
		if (seoRepo != null && seoRepo.getRedirectUrl() != null) {
			bloomreachResults.setSeoRedirectUrl((String) seoRepo.getRedirectUrl());
		}
		if (seoRepo != null && seoRepo.getHeaderContent() != null) {
			bloomreachResults.setHeaderContent((String) seoRepo.getHeaderContent());
		}
		if (seoRepo != null && seoRepo.getFooterContent() != null) {
			bloomreachResults.setSeoFooterText((String) seoRepo.getFooterContent());
		}
	}

	private String getBannersList(String qUri, Map<String, String> getBrandBannersMap) {
		log.debug(" BloomreachSearchDTOHelperImpl :: getBannersList START:: qUri: {}", qUri);
		String bannerName = null;
		String brannerUrl = qUri.replaceFirst(SearchConstants.SEARCH_CONTEXT_PATH, BloomreachConstants.EMPTY_STRING);
		if (customQueryBannersMap.containsKey(brannerUrl)) {
			bannerName = customQueryBannersMap.get(brannerUrl);
		}
		log.debug(" BloomreachSearchDTOHelperImpl:: getBannersList:: END bannerName::{}", bannerName);
		return bannerName;
	}
}
