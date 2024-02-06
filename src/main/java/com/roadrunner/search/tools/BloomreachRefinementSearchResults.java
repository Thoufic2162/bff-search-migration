package com.roadrunner.search.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.roadrunner.search.config.RRConfiguration;
import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.dto.BloomreachSearchRefinementsDTO;
import com.roadrunner.search.dto.BloomreachSearchResponseDTO;
import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.CatalogElementsFinder;
import com.roadrunner.search.dto.FacetData;
import com.roadrunner.search.dto.FacetFields;
import com.roadrunner.search.dto.FacetRanges;
import com.roadrunner.search.util.BloomreachSearchUtil;
import com.roadrunner.search.util.HttpUtil;
import com.roadrunner.search.util.StringUtil;
import com.roadrunner.search.util.URLCoderUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@ConfigurationProperties(prefix = "bloomreachrefinement")
@SuppressWarnings({ "unused" })
@Getter
@Setter
public class BloomreachRefinementSearchResults {

	private Map<String, String> navigationMap = new HashMap<>();
	private List<String> skipCategoryList;
	private List<String> skipClpUrl;
	private List<String> seoRefinmentList;
	private List<String> webPgcList;
	private Map<String, String> genderTypeMap;
	private Map<String, String> kidsTypeMap;
	private Map<String, String> seoFilterMap = new HashMap<>();
	private Map<String, String> refinementTitles = new HashMap<String, String>();

	private static final String USA_CODE = "USA";
	private static final String EURO_CODE = "EURO";

	@Autowired
	private BloomreachSearchUtil bloomreachSearchUtil;

	@Autowired
	private CatalogElementsFinder catalogElementsFinder;

	@Autowired
	private RRConfiguration rrConfiguration;

	public void getRefinementSearchResults(BloomreachSearchResponseDTO searchResults, HttpServletRequest request,
			BloomreachSearchResultsDTO responseBean) {
		log.debug(
				"BloomreachRefinementSearchResults :: getRefinementSearchResults() :: START :: searchResults {} request {} responseBean {}",
				searchResults, request, responseBean);
		if (searchResults == null) {
			log.debug(
					"BloomreachRefinementSearchResults :: getRefinementSearchResults() ::RefinementSearchResults: Results are empty");
			return;
		}
		Properties queryParams = HttpUtil.getRequestAttributesAndParameters(request);
		Map<String, List<BloomreachSearchRefinementsDTO>> refinements = doSearch(searchResults, queryParams, request,
				responseBean);
		responseBean.setRefinements(refinements);
		log.debug("BloomreachRefinementSearchResults :: getRefinementSearchResults() END :: responseBean {}",
				responseBean);
	}

	private Map<String, List<BloomreachSearchRefinementsDTO>> doSearch(BloomreachSearchResponseDTO searchResults,
			Properties pQueryParams, HttpServletRequest request, BloomreachSearchResultsDTO responseBean) {
		log.debug(
				"BloomreachRefinementSearchResults :: doSearch() :: START :: searchResults {} pQueryParams {} responseBean {}",
				searchResults, pQueryParams, responseBean);
		Properties queryParams = HttpUtil.getRequestAttributesAndParameters(request);
		String selNavs = SearchConstants.EMPTY_STRING;
		String pListParameter = request.getParameter(BloomreachConstants.LIST_PAGE);
		String qUri = URLCoderUtil.decode(request.getParameter(SearchConstants.QURI));
		boolean isGender = true;
		Boolean isSkipCategory = skipCategoryList.stream().anyMatch(url -> url.equals(qUri));
		if (null != qUri && qUri.contains(SearchConstants.CATEGORY_URL) && null != isSkipCategory && !isSkipCategory) {
			isGender = false;
		}
		Boolean isSkipClp = skipClpUrl.stream().allMatch(genderType -> !qUri.contains(genderType));
		if (null != qUri && qUri.contains(SearchConstants.CATEGORY_URL) && null != isSkipClp && isSkipClp) {
			isGender = true;
		}
		if (null != queryParams.get(BloomreachConstants.QPARAMS.R)) {
			selNavs = queryParams.get(BloomreachConstants.QPARAMS.R).toString();
		}
		String selRefinements = BloomreachConstants.QPARAMS.R + BloomreachConstants.EQUAL + selNavs;
		queryParams.put(SearchConstants.SELNAVS, selNavs);
		String selNavsActual = null;
		String[] selLeftNav = new String[0];
		selNavsActual = selNavs;
		selLeftNav = selNavsActual.split(SearchConstants.COLON);
		selRefinements = selRefinements.replaceAll(SearchConstants.SPACE, BloomreachConstants.PLUS);
		selRefinements = selRefinements.replaceAll(BloomreachConstants.URL_DELIMETER,
				BloomreachConstants.PERCENTAGE_26);
		String refUrl = selRefinements;

		if (!StringUtils.isEmpty(selNavs)) {
			refUrl += BloomreachConstants.PERCENTAGE_2C;
		}
		Map<String, String> selectedNavigationMap = bloomreachSearchUtil.selectedNavigation(selNavs, queryParams);
		String query = queryParams.getProperty(BloomreachConstants.QPARAMS.QUERY);
		Map<String, List<BloomreachSearchRefinementsDTO>> refinements = new LinkedHashMap<String, List<BloomreachSearchRefinementsDTO>>();
		List<BloomreachSearchRefinementsDTO> refs = null;
		Map<String, List<FacetData>> availableNavigation = availableNavigation(searchResults);
		for (Map.Entry<String, List<FacetData>> entry : availableNavigation.entrySet()) {
			// to remove the entire gender facet if the url is /category/socks
			boolean isSocksGenderFacet = (qUri != null && (qUri.contains(SearchConstants.CATEGORY_SOCKS_TOPNAV_URL)
					&& (entry.getKey().equals(BloomreachConstants.KIDS_GENDER)
							|| entry.getKey().equals(BloomreachConstants.GENDER_FACET)
							|| entry.getKey().equals(BloomreachConstants.CATEGORIES))));
			if (((qUri != null)
					&& (qUri.contains(SearchConstants.BRAND_TYPE_AHEAD_URL)
							&& entry.getKey().equals(BloomreachConstants.BRAND))
					|| !isGender && entry.getKey().equals(BloomreachConstants.GENDER_FACET)
					|| !isGender && entry.getKey().equals(BloomreachConstants.KIDS_GENDER)) || isSocksGenderFacet) {
				continue;
			}
			String url = refUrl;
			url += navigationMap.get(entry.getKey()) + URLCoderUtil.encode(SearchConstants.COLON);
			refinements.put(entry.getKey(), getRefinementDetails(refs, entry.getValue(), searchResults, query, url,
					entry.getKey(), queryParams, pListParameter, selectedNavigationMap, responseBean, request));
		}

		log.debug("BloomreachRefinementSearchResults :: doSearch() :: END :: refinements {}", refinements);
		return refinements;
	}

	public Map<String, List<FacetData>> availableNavigation(BloomreachSearchResponseDTO searchResults) {
		Map<String, List<FacetData>> list = new LinkedHashMap<>();
		if (null != searchResults.getFacetCounts()) {
			FacetFields facetFieldsList = searchResults.getFacetCounts().getFacetFields();
			FacetRanges priceList = searchResults.getFacetCounts().getFacetRanges();
			if (null != facetFieldsList) {
				log.debug("BloomreachRefinementSearchResults :: availableNavigation :: START :: facetFieldsList: {}",
						facetFieldsList);

				if (!CollectionUtils.isEmpty(facetFieldsList.getGenderText())) {
					List<FacetData> genderList = facetFieldsList.getGenderText();
					list.put(BloomreachConstants.GENDER_FACET, genderList);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getKidsGender())) {
					List<FacetData> kidsGender = facetFieldsList.getKidsGender();
					list.put(BloomreachConstants.KIDS_GENDER, kidsGender);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getKidsType())) {
					List<FacetData> kidsType = facetFieldsList.getKidsType();
					list.put(BloomreachConstants.AGE_GROUP, kidsType);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getWebPgc())) {
					List<FacetData> categoryList = facetFieldsList.getWebPgc();
					list.put(BloomreachConstants.CATEGORY, categoryList);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getShoeSize())) {
					List<FacetData> shoeSizeList = facetFieldsList.getShoeSize();
					list.put(BloomreachConstants.SHOE_SIZE, shoeSizeList);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getWidthGroup())) {
					List<FacetData> widthGroupList = facetFieldsList.getWidthGroup();
					list.put(BloomreachConstants.SHOE_WIDTH, widthGroupList);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getShoeType())) {
					List<FacetData> shoeTypeList = facetFieldsList.getShoeType();
					list.put(BloomreachConstants.SHOE_TYPE, shoeTypeList);
				}
				if (!CollectionUtils.isEmpty(priceList.getCustomerRatingList())) {
					List<FacetData> customerRatingList = priceList.getCustomerRatingList();
					list.put(BloomreachConstants.CUSTOMER_RATING, customerRatingList);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getWebSubPgc())) {
					List<FacetData> categoriesList = facetFieldsList.getWebSubPgc();
					list.put(BloomreachConstants.CATEGORIES, categoriesList);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getApparelType())) {
					List<FacetData> apparelType = facetFieldsList.getApparelType();
					list.put(BloomreachConstants.APPAREL_TYPE, apparelType);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getAccessoryType())) {
					List<FacetData> accessoryType = facetFieldsList.getAccessoryType();
					list.put(BloomreachConstants.ACCESSORY_TYPE, accessoryType);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getSockThickness())) {
					List<FacetData> sockThickness = facetFieldsList.getSockThickness();
					list.put(BloomreachConstants.SOCK_THICKNESS, sockThickness);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getSockLength())) {
					List<FacetData> sockLength = facetFieldsList.getSockLength();
					list.put(BloomreachConstants.SOCK_LENGTH, sockLength);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getBrand())) {
					List<FacetData> brandList = facetFieldsList.getBrand();
					list.put(BloomreachConstants.BRAND, brandList);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getSports())) {
					List<FacetData> sportsList = facetFieldsList.getSports();
					list.put(BloomreachConstants.SPORTS, sportsList);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getApparelSize())) {
					List<FacetData> apparelSize = facetFieldsList.getApparelSize();
					list.put(BloomreachConstants.SIZE, apparelSize);
				}
				if (null != priceList && !CollectionUtils.isEmpty(priceList.getRegPriceRangeList())) {
					List<FacetData> price = priceList.getRegPriceRangeList();
					list.put(BloomreachConstants.PRICE, price);
				}

				if (!CollectionUtils.isEmpty(facetFieldsList.getColorGroup())) {
					List<FacetData> colorGroupList = facetFieldsList.getColorGroup();
					list.put(BloomreachConstants.COLOR, colorGroupList);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getShoeCushion())) {
					List<FacetData> shoeCushionList = facetFieldsList.getShoeCushion();
					list.put(BloomreachConstants.SHOE_CUSHION_LEVELS, shoeCushionList);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getSale())) {
					List<FacetData> saleList = facetFieldsList.getSale();
					list.put(BloomreachConstants.SALE, saleList);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getNewProduct())) {
					List<FacetData> newArrivalsList = facetFieldsList.getNewProduct();
					list.put(BloomreachConstants.NEW_ARRIVALS, newArrivalsList);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getExclusive())) {
					facetFieldsList.getExclusive().get(0).setName(BloomreachConstants.EXCLUSIVE);
					List<FacetData> exclusive = facetFieldsList.getExclusive();
					list.put(BloomreachConstants.EXCLUSIVE, exclusive);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getSale())) {
					List<FacetData> sale = facetFieldsList.getSale();
					list.put(BloomreachConstants.SALE, sale);
				}
				if (!CollectionUtils.isEmpty(facetFieldsList.getOutlet())) {
					List<FacetData> outletList = facetFieldsList.getOutlet();
					list.put(BloomreachConstants.OUTLET, outletList);
				}

				if (!CollectionUtils.isEmpty(facetFieldsList.getOutletBasement())) {
					List<FacetData> outletBasementList = facetFieldsList.getOutletBasement();
					list.put(BloomreachConstants.OUTLET_BASEMENT, outletBasementList);
				}

				if (!CollectionUtils.isEmpty(facetFieldsList.getInjuryPlacement())) {
					List<FacetData> injuryPlacementList = facetFieldsList.getInjuryPlacement();
					list.put(BloomreachConstants.INJURY_PLACEMENT, injuryPlacementList);
				}

				if (!CollectionUtils.isEmpty(facetFieldsList.getInjuryRecovery())) {
					List<FacetData> injuryRecoveryList = facetFieldsList.getInjuryRecovery();
					list.put(BloomreachConstants.INJURY_RECOVERY, injuryRecoveryList);
				}
			}

		}
		log.debug("BloomreachRefinementSearchResults :: availableNavigation :: END :: list: {}", list);
		return list;
	}

	private List<BloomreachSearchRefinementsDTO> getRefinementDetails(List<BloomreachSearchRefinementsDTO> refs,
			List<FacetData> pValue, BloomreachSearchResponseDTO pSearchResults, String pQuery, String pRefUrl,
			String pKey, Properties pQueryParams, String pListParameter, Map<String, String> pSelectedNavigationMap,
			BloomreachSearchResultsDTO pResponseBean, HttpServletRequest pRequest) {
		log.debug("BloomreachRefinementSearchResults :: getRefinementDetails() :: START ::");
		log.debug(
				"refs {} pValue {} pSearchResults {} pQuery {} pRefUrl {} pKey {} "
						+ "pQueryParams {} pListParameter {} pSelectedNavigationMap {} pResponseBean {}",
				refs, pValue, pSearchResults, pQuery, pRefUrl, pKey, pQueryParams, pListParameter,
				pSelectedNavigationMap, pResponseBean);
		refs = new ArrayList<BloomreachSearchRefinementsDTO>();
		String rParam = null;

		if (null != pQueryParams.get(BloomreachConstants.QPARAMS.R)) {
			rParam = pQueryParams.get(BloomreachConstants.QPARAMS.R).toString();
		}
		buildRefinementDetails(refs, pValue, pSearchResults, pQuery, pRefUrl, pKey, pQueryParams,
				pSelectedNavigationMap);
		if (StringUtils.isNotEmpty((String) pQueryParams.get(SearchConstants.DYNAMIC_URL_REFINMENT))
				&& pQueryParams.get(SearchConstants.DYNAMIC_URL_REFINMENT).equals(SearchConstants.TRUE)) {
			refs.stream().forEach(refinementBean -> {
				if (!refinementBean.getUrl().contains(SearchConstants.REF)) {
					refinementBean.setUrl(refinementBean.getUrl().replace(SearchConstants.REF_L, SearchConstants.REF));
				}
			});
		}
		if (seoRefinmentList.stream().anyMatch(navigationMap.get(pKey)::equalsIgnoreCase)) {
			if (StringUtils.isNotEmpty((String) pQueryParams.get(SearchConstants.DYNAMIC_URL_REFINMENT))
					&& pQueryParams.get(SearchConstants.DYNAMIC_URL_REFINMENT).equals(SearchConstants.TRUE)
					&& !refs.stream().anyMatch(ref -> ref.getState().equals(SearchConstants.ACTIVE))) {
				refs.stream().forEach(ref -> {
					String urlQuery = (String) pQueryParams.get(SearchConstants.URL_QUERY);
					String brand = BloomreachConstants.EMPTY_STRING;
					if (!urlQuery.isEmpty()) {
						if (!urlQuery.endsWith(SearchConstants.SLASH)) {
							urlQuery = urlQuery + SearchConstants.SLASH;
						}
						StringBuilder url = new StringBuilder(SearchConstants.EMPTY_STRING);
						brand = ref.getName().replaceAll(BloomreachConstants.SINGLE_QUOTES, BloomreachConstants.HYPHEN);
						url.append(urlQuery);
						if (catalogElementsFinder.getWebPgcMap().containsKey(ref.getName().toLowerCase()
								.replace(SearchConstants.SPACE, SearchConstants.HYPHEN_STRING))) {
							url.append(catalogElementsFinder.getWebPgcMap().get(ref.getName().toLowerCase()
									.replace(SearchConstants.SPACE, SearchConstants.HYPHEN_STRING)));
						} else {
							url.append(
									brand.toLowerCase().replace(SearchConstants.SPACE, SearchConstants.HYPHEN_STRING));
						}
						if (urlQuery.contains(BloomreachConstants.OUTLET_PARAM)
								&& null == pResponseBean.getClearRefUrl()) {
							pResponseBean.setClearRefUrl(outletClearUrl(url));
						}
						reorderUrl(url);
						addQueryParam(pKey, pQueryParams, url);
						String sort = BloomreachConstants.EMPTY_STRING;
						if (null != pQueryParams.get(BloomreachConstants.QPARAMS.S)) {
							sort = BloomreachConstants.URL_DELIMETER.concat(BloomreachConstants.QPARAMS.S)
									.concat(BloomreachConstants.EQUAL)
									.concat(pQueryParams.get(BloomreachConstants.QPARAMS.S).toString());
						}
						String rqueryUrl = url.toString();
						rqueryUrl = rqueryUrl.replaceAll(SearchConstants.SPACE, SearchConstants.PLUS)
								.replaceAll(BloomreachConstants.PERCENTAGE_26, SearchConstants.URL_DELIMETER2);
						StringBuilder urlBuilder = new StringBuilder();
						urlBuilder.append(rqueryUrl).append(sort);
						url = urlBuilder;
						ref.setUrl(url.toString());
						if (!ref.getUrl().contains(SearchConstants.URL_DELIMETER2) && ref.getProducts() >= 3
								&& !isFifthLevelUrl(ref.getUrl())) {
							ref.setSeoUrl(true);
						}

					}
				});
			}

			if (StringUtils.isNotEmpty((String) pQueryParams.get(SearchConstants.DYNAMIC_URL_REFINMENT))
					&& pQueryParams.get(SearchConstants.DYNAMIC_URL_REFINMENT).equals(SearchConstants.TRUE)
					&& refs.stream().anyMatch(ref -> ref.getState().equals(SearchConstants.ACTIVE))) {
				List<String> activeBrandList = new ArrayList<String>();

				refs.stream().filter(ref -> ref.getState().equals(SearchConstants.ACTIVE)).forEach(ref -> {
					String brand = ref.getName();
					if (catalogElementsFinder.getWebPgcMap().containsKey(
							brand.toLowerCase().replace(SearchConstants.SPACE, SearchConstants.HYPHEN_STRING))) {
						activeBrandList.add(catalogElementsFinder.getWebPgcMap().get(
								brand.toLowerCase().replace(SearchConstants.SPACE, SearchConstants.HYPHEN_STRING)));
					} else {
						activeBrandList
								.add(brand.toLowerCase().replace(SearchConstants.SPACE, SearchConstants.HYPHEN_STRING));
					}
				});
				refs.stream().forEach(ref -> {
					String urlQuery = (String) pQueryParams.get(SearchConstants.URL_QUERY);
					if (!urlQuery.isEmpty()) {
						if (!urlQuery.endsWith(SearchConstants.SLASH)) {
							urlQuery = urlQuery + SearchConstants.SLASH;
						}
						if (urlQuery.contains(SearchConstants.TRISUITS)) {
							urlQuery = urlQuery.replace(SearchConstants.TRISUITS, SearchConstants.UNISUITS);
						}
						List<String> iActiveBrandList = new LinkedList<>(activeBrandList);
						StringBuilder url = new StringBuilder(BloomreachConstants.EMPTY_STRING);
						for (String activeBrand : activeBrandList) {
							urlQuery = urlQuery.replace(activeBrand, SearchConstants.EMPTY_STRING)
									.replace(SearchConstants.DOUBLE_SLASH, SearchConstants.SLASH);
						}
						String urlKey = ref.getName().toLowerCase().replace(SearchConstants.SPACE,
								SearchConstants.HYPHEN_STRING);

						if (catalogElementsFinder.getWebPgcMap().containsKey(urlKey)) {
							urlKey = catalogElementsFinder.getWebPgcMap().get(urlKey);
						}
						String urlString = urlQuery.replace(urlKey, SearchConstants.EMPTY_STRING)
								.replace(SearchConstants.DOUBLE_SLASH, SearchConstants.SLASH);
						if (null != webPgcList) {
							for (String webPgc : webPgcList) {
								if (urlString.contains(webPgc)) {
									urlString = urlQuery.replace(webPgc, SearchConstants.EMPTY_STRING)
											.replace(SearchConstants.DOUBLE_SLASH, SearchConstants.SLASH);
								}
							}
						}
						url.append(urlString);
						String brand = ref.getName().toLowerCase().replace(SearchConstants.SPACE,
								SearchConstants.HYPHEN_STRING);
						if (catalogElementsFinder.getWebPgcMap().containsKey(brand)) {
							brand = catalogElementsFinder.getWebPgcMap().get(brand);
						}
						if (!iActiveBrandList.contains(brand)) {
							iActiveBrandList.add(brand);
						} else {
							iActiveBrandList.remove(brand);
						}
						if (iActiveBrandList.size() == 1) {
							url.append(iActiveBrandList.get(0));
							url.append(SearchConstants.SLASH);
						}
						if (urlString.contains(BloomreachConstants.OUTLET_PARAM)
								&& null == pResponseBean.getClearRefUrl()) {
							pResponseBean.setClearRefUrl(outletClearUrl(url));
						}
						reorderUrl(url);
						String sort = BloomreachConstants.EMPTY_STRING;
						if (null != pQueryParams.get(BloomreachConstants.QPARAMS.S)) {
							sort = BloomreachConstants.URL_DELIMETER.concat(BloomreachConstants.QPARAMS.S)
									.concat(BloomreachConstants.EQUAL)
									.concat(pQueryParams.get(BloomreachConstants.QPARAMS.S).toString());
						}
						if (!iActiveBrandList.isEmpty() && iActiveBrandList.size() != 1) {
							if (url.toString().equals(SearchConstants.CATEGORY_U)
									&& navigationMap.get(pKey).equalsIgnoreCase(SearchConstants.BRAND)) {
								url.append(SearchConstants.SLASH);
								Optional<String> brandVal = iActiveBrandList.stream().findFirst();
								if (brandVal.isPresent()) {
									url.append(brandVal.get());
									iActiveBrandList.remove(0);
								}
							}
							url.append(SearchConstants.URL_DELIMETER2);
							if (navigationMap.get(pKey).equalsIgnoreCase(SearchConstants.BRAND)) {
								url.append(SearchConstants.BRAND_PA);
							} else if (navigationMap.get(pKey).equalsIgnoreCase(SearchConstants.VARIANTS_COLORGROUP)) {
								url.append(SearchConstants.COLOR_PA);
							} else if (navigationMap.get(pKey)
									.equalsIgnoreCase(SearchConstants.WEB_PGC_SUB_CODE_PARAM)) {
								url.append(SearchConstants.CAT_PA);
							} else if (navigationMap.get(pKey).equalsIgnoreCase(SearchConstants.SHOE_TYPE)) {
								url.append(SearchConstants.SHOE_PA);
							} else if (navigationMap.get(pKey).equalsIgnoreCase(SearchConstants.SPORTS_TYPE)) {
								url.append(SearchConstants.SPORTS_EQUALS);
							}
							url.append(String.join(BloomreachConstants.COMMA, iActiveBrandList));
						}
						addQueryParam(pKey, pQueryParams, url);
						String rqueryUrl = url.toString();
						rqueryUrl = rqueryUrl.replaceAll(SearchConstants.SPACE, SearchConstants.PLUS)
								.replaceAll(SearchConstants.STRING_26, SearchConstants.URL_DELIMETER2);

						StringBuilder builder = new StringBuilder();
						builder.append(rqueryUrl).append(sort);
						url = builder;
						ref.setUrl(url.toString());
						String urlStr = url.toString();
						if (iActiveBrandList.contains(SearchConstants.UNISUITS) && (activeBrandList.size() > 0)) {
							String replaceString = urlStr.replace(SearchConstants.UNISUITS_SLASH,
									SearchConstants.EMPTY_STRING);
							ref.setUrl(replaceString);
						}
					}
				});
			}
		}
		if (navigationMap.get(pKey).equalsIgnoreCase(BloomreachConstants.PRODUCT_FIELD.BRAND)) {
			String qUri = null;
			if (pRequest.getParameter(SearchConstants.QURI) != null) {
				qUri = URLCoderUtil.decode(pRequest.getParameter(SearchConstants.QURI));
			}
			refs.stream().forEach(ref -> {
				if (ref.getBaseUrl() != null && !ref.getBaseUrl().isEmpty()) {
					ref.setUrl(ref.getBaseUrl());
				}
			});
			refs.sort((BloomreachSearchRefinementsDTO o1, BloomreachSearchRefinementsDTO o2) -> o1.getName()
					.compareToIgnoreCase(o2.getName()));
			if (qUri != null && qUri.contains(SearchConstants.APPARELURL)) {
				for (BloomreachSearchRefinementsDTO s : refs) {
					if (s.getName().equalsIgnoreCase(SearchConstants.KORSA)) {
						int index = refs.indexOf(s);
						refs.remove(index);
						refs.add(0, s);
						break;
					}
				}
			}
			if (qUri != null && qUri.contains(SearchConstants.ACCESSORIESURL)) {
				for (BloomreachSearchRefinementsDTO s : refs) {
					if (s.getName().equalsIgnoreCase(SearchConstants.RGEAR)) {
						int index = refs.indexOf(s);
						refs.remove(index);
						refs.add(0, s);
						break;
					}
				}
			}
			if (qUri != null && qUri.contains(SearchConstants.CATEGORY_SOCKS_TOPNAV_URL)) {
				for (BloomreachSearchRefinementsDTO s : refs) {
					if (s.getName().equalsIgnoreCase(SearchConstants.RGEAR)) {
						int index = refs.indexOf(s);
						refs.remove(index);
						refs.add(0, s);
						break;
					}
				}
			}
			return refs;
		}
		if ((null != pQuery && !pQuery.contains(SearchConstants.KIDS))
				|| (null != rParam && !rParam.contains(SearchConstants.KIDS))) {
			if (navigationMap.get(pKey).equalsIgnoreCase(BloomreachConstants.SKU_FIELD.VARIANTS_SHOE_SIZE)) {
				List<BloomreachSearchRefinementsDTO> sizeSortedRefSizes = new ArrayList<BloomreachSearchRefinementsDTO>();
				for (BloomreachSearchRefinementsDTO refBean : refs) {
					if (null != refBean.getName()) {
						double size = StringUtil.getDoubleFromString(refBean.getName().trim());
						if (NumberUtils.isParsable(refBean.getName())) {
							if (size <= 0.0 || size > 20.0) {
								refBean.setSizeCode(EURO_CODE);
							} else {
								refBean.setSizeCode(USA_CODE);
							}
							sizeSortedRefSizes.add(refBean);
						}
					}
				}
				Collections.sort(sizeSortedRefSizes,
						Comparator.comparingDouble(sorting -> Double.parseDouble(sorting.getName())));
				return sizeSortedRefSizes;

			}
		}

		if (navigationMap.get(pKey).equalsIgnoreCase(BloomreachConstants.PRODUCT_FIELD.GENDER_TEXT)) {
			String qUri = null;
			if (pRequest.getParameter(SearchConstants.QURI) != null) {
				qUri = URLCoderUtil.decode(pRequest.getParameter(SearchConstants.QURI));
			}
			if (qUri != null && qUri.contains(SearchConstants.CATEGORY_URL)
					&& !qUri.contains(SearchConstants.TYPE_AHEAD_SEARCH_QUERY)) {
				for (BloomreachSearchRefinementsDTO item : refs) {
					StringBuffer newUrl = new StringBuffer(qUri);
					if (item.getName() != null && (item.getName()
							.replaceAll(SearchConstants.APOSTROPHE, BloomreachConstants.EMPTY_STRING).toLowerCase()
							.equalsIgnoreCase(SearchConstants.MENS)
							|| item.getName().replaceAll(SearchConstants.APOSTROPHE, BloomreachConstants.EMPTY_STRING)
									.toLowerCase().equalsIgnoreCase(SearchConstants.WOMENS)
							|| item.getName().replaceAll(SearchConstants.APOSTROPHE, BloomreachConstants.EMPTY_STRING)
									.toLowerCase().equalsIgnoreCase(SearchConstants.KIDS))) {
						item.setUrl(newUrl.insert(10, genderTypeMap.get(item.getName())).toString());
					}
				}
			}

		}
		if (navigationMap.get(pKey).equalsIgnoreCase(BloomreachConstants.PRODUCT_FIELD.KIDS_GENDER)) {
			String qUri = null;
			if (pRequest.getParameter(SearchConstants.QURI) != null) {
				qUri = URLCoderUtil.decode(pRequest.getParameter(SearchConstants.QURI));
			}
			if (qUri != null && qUri.contains(SearchConstants.CATEGORY_URL)
					&& !qUri.contains(SearchConstants.TYPE_AHEAD_SEARCH_QUERY)) {
				for (BloomreachSearchRefinementsDTO item : refs) {
					StringBuffer newUrl = new StringBuffer(qUri);
					if (item.getName() != null && (item.getName()
							.replaceAll(SearchConstants.APOSTROPHE, BloomreachConstants.EMPTY_STRING).toLowerCase()
							.equalsIgnoreCase(SearchConstants.BOYS)
							|| item.getName().replaceAll(SearchConstants.APOSTROPHE, BloomreachConstants.EMPTY_STRING)
									.toLowerCase().equalsIgnoreCase(SearchConstants.GIRLS))) {
						item.setUrl(newUrl.insert(10, kidsTypeMap.get(item.getName())).toString());
					}
				}
			}

		}
		log.debug("BloomreachRefinementSearchResults :: getRefinementDetails() :: ListOf RefSizes {}: ", refs);
		return refs;
	}

	private String outletClearUrl(StringBuilder pUrl) {
		log.debug("BloomreachRefinementSearchResults :: outletClearUrl() :: START :: url: {}", pUrl);
		StringBuilder clearfilterUrl = new StringBuilder(pUrl);
		String urlString = pUrl.toString();
		String[] aUrls = urlString.split(SearchConstants.SLASH);
		Map<String, Integer> urlmap = new HashMap<String, Integer>();
		for (String aUrl : aUrls) {
			if (BloomreachConstants.CATEGORY.equalsIgnoreCase(aUrl)) {
				urlmap.put(aUrl, 0);
				continue;
			}
			if (catalogElementsFinder.getGender().contains(aUrl)) {
				urlmap.put(aUrl, 1);
				continue;
			}
			if (catalogElementsFinder.getCategory().contains(aUrl)) {
				urlmap.put(aUrl, 2);
				continue;
			}
			if (catalogElementsFinder.getBrands().contains(aUrl)
					&& urlString.contains(pUrl + BloomreachConstants.OUTLET_PARAM)) {
				urlmap.put(aUrl, 3);
				continue;
			}
			if (SearchConstants.OUTLET.equalsIgnoreCase(aUrl)) {
				urlmap.put(aUrl, 4);
				continue;
			}
			if (SearchConstants.BASEMENT.equalsIgnoreCase(aUrl)) {
				urlmap.put(aUrl, 5);
				continue;
			}
		}
		List<String> sortedurl = urlmap.entrySet().stream().sorted(Entry.comparingByValue()).map(Map.Entry::getKey)
				.collect(Collectors.toList());

		if (!CollectionUtils.isEmpty(sortedurl) && sortedurl.size() > 3) {
			sortedurl.remove(sortedurl.size() - 1);
		}
		clearfilterUrl.setLength(0);
		clearfilterUrl.append(SearchConstants.SLASH);
		clearfilterUrl.append(String.join(SearchConstants.SLASH, sortedurl));
		log.debug("BloomreachRefinementSearchResults :: outletClearUrl() :: END :: clearfilterUrl: {}", clearfilterUrl);
		return clearfilterUrl.toString();
	}

	private void reorderUrl(StringBuilder url) {
		log.debug("BloomreachRefinementSearchResults::reorderUrl::START::url::{}", url);
		String urlString = url.toString();
		String[] aUrls = urlString.split(SearchConstants.SLASH);
		Map<String, Integer> urlmap = new HashMap<String, Integer>();
		for (String aUrl : aUrls) {
			boolean isKids = false;
			if (urlString.contains(SearchConstants.SITE_KIDS)) {
				isKids = true;
			}
			if (BloomreachConstants.CATEGORY.equalsIgnoreCase(aUrl)) {
				urlmap.put(aUrl, 0);
				continue;
			}
			if (SearchConstants.SITE_KIDS.equalsIgnoreCase(aUrl)) {
				urlmap.put(aUrl, 1);
				continue;
			}
			if (catalogElementsFinder.getGender().contains(aUrl)) {
				urlmap.put(aUrl, isKids ? 2 : 1);
				continue;
			}
			if (catalogElementsFinder.getCategory().contains(aUrl)) {
				urlmap.put(aUrl, isKids ? 3 : 2);
				continue;
			}
			if (catalogElementsFinder.getSubCategory().contains(aUrl)) {
				urlmap.put(aUrl, isKids ? 4 : 3);
				continue;
			}
			if (catalogElementsFinder.getBrands().contains(aUrl)) {
				urlmap.put(aUrl, isKids ? 6 : 5);
				continue;
			}
			if (SearchConstants.OUTLET.equalsIgnoreCase(aUrl)) {
				urlmap.put(aUrl, isKids ? 10 : 9);
				continue;
			}
			if (SearchConstants.BASEMENT.equalsIgnoreCase(aUrl)) {
				urlmap.put(aUrl, isKids ? 9 : 8);
				continue;
			}
			if (SearchConstants.NEW.equalsIgnoreCase(aUrl)) {
				urlmap.put(aUrl, isKids ? 5 : 4);
				continue;
			}
			if (SearchConstants.SALE.equalsIgnoreCase(aUrl)) {
				urlmap.put(aUrl, isKids ? 5 : 4);
				continue;
			}
			if (SearchConstants.PERSONALCARE.equalsIgnoreCase(aUrl)) {
				urlmap.put(aUrl, isKids ? 3 : 2);
				continue;
			}
			if (SearchConstants.EQUIPMENT.equalsIgnoreCase(aUrl)) {
				urlmap.put(aUrl, isKids ? 3 : 2);
				continue;
			}

			if (SearchConstants.LAST_CHANCE.equalsIgnoreCase(aUrl)) {
				urlmap.put(aUrl, isKids ? 5 : 6);
				continue;
			}
			if (catalogElementsFinder.getColors().contains(aUrl)) {
				urlmap.put(aUrl, isKids ? 7 : 6);
				continue;
			}
			if (catalogElementsFinder.getSize().contains(aUrl)) {
				urlmap.put(aUrl, isKids ? 5 : 4);
				continue;
			}
			if (catalogElementsFinder.getShoeType().contains(aUrl)) {
				urlmap.put(aUrl, isKids ? 8 : 7);
				continue;
			}
			if (catalogElementsFinder.getSportsMap().entrySet().stream()
					.anyMatch(sports -> sports.getValue().equalsIgnoreCase(aUrl))) {
				urlmap.put(aUrl, isKids ? 11 : 10);
				continue;
			}

		}
		List<String> sortedurl = urlmap.entrySet().stream().sorted(Entry.comparingByValue()).map(Map.Entry::getKey)
				.collect(Collectors.toList());

		url.setLength(0);
		url.append(SearchConstants.SLASH);
		url.append(String.join(SearchConstants.SLASH, sortedurl));
		log.debug("BloomreachRefinementSearchResults::reorderUrl::END::url::{}", url);
	}

	private void addQueryParam(String key, Properties queryParams, StringBuilder url) {
		log.debug("BloomreachRefinementSearchResults::addQueryParam START :: key::{}::queryParams::{1}::url::{2}", key,
				queryParams, url);
		if (navigationMap.get(key).equalsIgnoreCase(SearchConstants.BRAND)) {
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.COLOR))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.COLOR_PA);
				url.append(queryParams.get(SearchConstants.COLOR));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.CAT))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.CAT_PA);
				url.append(queryParams.get(SearchConstants.CAT));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.SHOE_TYPE))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.SHOE_PA);
				url.append(queryParams.get(SearchConstants.SHOE_TYPE));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.SPORTS_TYPE))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.SPORTS_EQUALS);
				url.append(queryParams.get(SearchConstants.SPORTS_TYPE));
			}
		} else if (navigationMap.get(key).equalsIgnoreCase(SearchConstants.VARIANTS_COLORGROUP)) {
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.BRAND))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.BRAND_PA);
				url.append(queryParams.get(SearchConstants.BRAND));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.CAT))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.CAT_PA);
				url.append(queryParams.get(SearchConstants.CAT));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.SHOE_TYPE))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.SHOE_PA);
				url.append(queryParams.get(SearchConstants.SHOE_TYPE));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.SPORTS_TYPE))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.SPORTS_EQUALS);
				url.append(queryParams.get(SearchConstants.SPORTS_TYPE));
			}
		} else if (navigationMap.get(key).equalsIgnoreCase(SearchConstants.WEB_PGC_SUB_CODE_PARAM)) {
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.COLOR))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.COLOR_PA);
				url.append(queryParams.get(SearchConstants.COLOR));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.BRAND))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.BRAND_PA);
				url.append(queryParams.get(SearchConstants.BRAND));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.SHOE_TYPE))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.SHOE_PA);
				url.append(queryParams.get(SearchConstants.SHOE_TYPE));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.SPORTS_TYPE))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.SPORTS_EQUALS);
				url.append(queryParams.get(SearchConstants.SPORTS_TYPE));
			}
		} else if (navigationMap.get(key).equalsIgnoreCase(SearchConstants.SHOE_TYPE)) {
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.COLOR))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.COLOR_PA);
				url.append(queryParams.get(SearchConstants.COLOR));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.BRAND))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.BRAND_PA);
				url.append(queryParams.get(SearchConstants.BRAND));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.CAT))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.CAT_PA);
				url.append(queryParams.get(SearchConstants.CAT));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.SPORTS_TYPE))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.SPORTS_EQUALS);
				url.append(queryParams.get(SearchConstants.SPORTS_TYPE));
			}
		} else if (navigationMap.get(key).equalsIgnoreCase(SearchConstants.SPORTS_TYPE)) {
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.COLOR))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.COLOR_PA);
				url.append(queryParams.get(SearchConstants.COLOR));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.BRAND))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.BRAND_PA);
				url.append(queryParams.get(SearchConstants.BRAND));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.CAT))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.CAT_PA);
				url.append(queryParams.get(SearchConstants.CAT));
			}
			if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.SHOE_TYPE))) {
				url.append(SearchConstants.URL_DELIMETER2);
				url.append(SearchConstants.SHOE_PA);
				url.append(queryParams.get(SearchConstants.SHOE_TYPE));
			}
		}

		if (StringUtils.isNotEmpty((String) queryParams.get(SearchConstants.R_QUERY))) {
			url.append(SearchConstants.URL_DELIMETER2);
			url.append(SearchConstants.REF_L);
			url.append(queryParams.get(SearchConstants.R_QUERY));
		}
		log.debug("BloomreachRefinementSearchResults:: END addQueryParam::url::{}", url);
	}

	private boolean isFifthLevelUrl(String url) {
		String[] urlV = url.split(SearchConstants.SLASH);
		boolean isFifthLevel = false;
		int urlLength = urlV.length;
		if (url.contains(SearchConstants.KIDS_BASE_CONTEXT_PATH)) {
			if (urlLength >= 8) {
				isFifthLevel = true;
			}
		} else {
			if (urlLength >= 7) {
				isFifthLevel = true;
			}
		}
		return isFifthLevel;
	}

	public void buildRefinementDetails(List<BloomreachSearchRefinementsDTO> refs, List<FacetData> qwAnswers,
			BloomreachSearchResponseDTO pSearchResults, String searchQuery, String selRefinements, String pString,
			Properties queryParams, Map<String, String> pSelectedNavigationMap) {
		BloomreachSearchRefinementsDTO refinementBean = null;
		log.debug(
				"BloomreachRefinementSearchResults :: buildRefinementDetails() STARTED with refs {} qwAnswers{}  selRefinements{}",
				refs, qwAnswers, selRefinements);
		if (qwAnswers != null && qwAnswers.size() > 0) {
			String text = null;
			String start = null;
			String end = null;
			String url = SearchConstants.HASH;
			String refURL = BloomreachConstants.EMPTY_STRING;
			boolean isPrice = false;
			String refName = null;
			String refUrl = null;
			String refinmentSection = null;
			String urlKidsType = null;
			String urlQuery = null;
			int count = 0;
			Optional<String> searchColorName = null;
			if (null != searchQuery) {
				searchColorName = Arrays
						.stream(searchQuery
								.replaceAll(SearchConstants.SEARCH_CONTEXT_PATH, BloomreachConstants.EMPTY_STRING)
								.split(SearchConstants.SPACE))
						.filter(isColor -> catalogElementsFinder.getColors().stream()
								.anyMatch(clr -> (isColor.equalsIgnoreCase(clr))))
						.findAny();
			}
			for (FacetData objAnswer : qwAnswers) {
				boolean disabledRef = false;
				boolean isExclude = false;
				String searchRefinementQuery = null;
				String searchColor = queryParams.getProperty(SearchConstants.SEARCH_COLOR);
				String searchBrand = queryParams.getProperty(SearchConstants.SEARCH_BRAND);
				String searchGender = queryParams.getProperty(SearchConstants.SEARCH_GENDER);
				String searchShoeSize = queryParams.getProperty(BloomreachConstants.SHOE_SIZE_PARAM);
				String searchApparelSize = queryParams.getProperty(BloomreachConstants.APPAREL_SIZE);
				if (objAnswer instanceof FacetData) {
					FacetData qAnswer = (FacetData) objAnswer;
					text = qAnswer.getName();
					count = qAnswer.getCount();
					start = qAnswer.getStart();
					end = qAnswer.getEnd();
					boolean isSearchColor = (pString.equalsIgnoreCase(BloomreachConstants.COLOR) && null != searchColor
							&& searchColor.contains(text.toLowerCase()));
					boolean isSearchBrand = (null != searchBrand && searchBrand.equalsIgnoreCase(text));
					boolean isSearchGender = (null != searchGender && searchGender.equalsIgnoreCase(text));
					boolean isShoeSize = (null != searchShoeSize
							&& searchShoeSize.replace(SearchConstants.SIZE_WITH_SPACE, BloomreachConstants.EMPTY_STRING)
									.equalsIgnoreCase(text));
					boolean isApparelSize = (null != searchApparelSize && null != text && catalogElementsFinder
							.getApparelSizeMap().get(searchApparelSize).equalsIgnoreCase(text.toLowerCase()));
					searchRefinementQuery = isSearchDefaultRefinement(searchQuery, text, searchColorName, isSearchBrand,
							isSearchGender, isShoeSize, isApparelSize, queryParams);
					urlQuery = queryParams.getProperty(SearchConstants.URL_QUERY);

					if (null != pString && (pString.equalsIgnoreCase(BloomreachConstants.PRICE))) {
						if (end.contains(SearchConstants.STAR)) {
							text = BloomreachConstants.STRING_OVER + SearchConstants.SPACE + BloomreachConstants.DOLLER
									+ start;
						} else {
							text = BloomreachConstants.DOLLER + start + BloomreachConstants.HYPHEN_WITH_SPACE
									+ BloomreachConstants.DOLLER + end;
						}
						refUrl = populatePriceRange(start, end);
						isPrice = true;
					}

					if (null != pString && pString.equalsIgnoreCase(BloomreachConstants.CUSTOMER_RATING)) {
						refUrl = populatePriceRange(start, end);
						isPrice = true;
						text = start.concat(BloomreachConstants.START_UP);
						if (start.equalsIgnoreCase(BloomreachConstants.ZERO)) {
							continue;
						}
					}
					if (null != urlQuery && pString.equalsIgnoreCase(SearchConstants.SPORTS_TYPE)
							&& catalogElementsFinder.getSportsMap().containsKey(text)) {
						text = catalogElementsFinder.getSportsMap().get(text);
					}
					for (Map.Entry<String, String> entry : pSelectedNavigationMap.entrySet()) {
						String selectedRef = isPrice ? refUrl : text;
						if (entry.getKey().equalsIgnoreCase(selectedRef)) {
							isExclude = true;
							refinmentSection = entry.getValue().concat(BloomreachConstants.PERCENTAGE_3A);
						}
					}
					if (((isSearchColor || isSearchBrand || isSearchGender || isApparelSize || isShoeSize))) {
						isExclude = true;
					}
					selRefinements = selRefinements.replaceAll(SearchConstants.COLON, BloomreachConstants.PERCENTAGE_3A)
							.replaceAll(BloomreachConstants.COMMA, BloomreachConstants.PERCENTAGE_2C)
							.replaceAll(BloomreachConstants.SINGLE_QUOTES, BloomreachConstants.PERCENTAGE_27)
							.replaceAll(BloomreachConstants.STRING_PLUS, BloomreachConstants.PERCENTAGE_20)
							.replaceAll(BloomreachConstants.PERCENTAGE_2C2C, BloomreachConstants.PERCENTAGE_2C)
							.replaceAll(SearchConstants.SLASH, BloomreachConstants.PERCENTAGE_2F);

					if (null != queryParams.get(BloomreachConstants.BR_QUERY_PARAM_DYNAMIC_URL_REFINMENT)) {
						String removeSelRefinements = (String) queryParams
								.get(BloomreachConstants.BR_QUERY_PARAM_DYNAMIC_URL_REFINMENT);
						removeSelRefinements = removeSelRefinements
								.replaceAll(BloomreachConstants.PERCENTAGE_2B, BloomreachConstants.PERCENTAGE_20)
								.replaceAll(BloomreachConstants.STRING_PLUS, BloomreachConstants.PERCENTAGE_20)
								.replaceAll(BloomreachConstants.PERCENTAGE_2526, BloomreachConstants.PERCENTAGE_26)
								.replaceAll(BloomreachConstants.PERCENTAGE_2528, BloomreachConstants.PERCENTAGE_28)
								.replaceAll(BloomreachConstants.PERCENTAGE_2529, BloomreachConstants.PERCENTAGE_29);
						selRefinements = selRefinements.replaceAll(removeSelRefinements,
								BloomreachConstants.EMPTY_STRING);

					}
					refName = isPrice ? URLCoderUtil.encode(refUrl) : URLCoderUtil.encode(text);
					if (!isExclude) {
						refURL = selRefinements + refName;
					} else {
						disabledRef = true;
						String selectedRefinement = navigationMap.get(pString)
								+ URLCoderUtil.encode(SearchConstants.COLON)
								+ refName.replaceAll(BloomreachConstants.PERCENTAGE_20, BloomreachConstants.STRING_PLUS)
										.replaceAll(BloomreachConstants.PERCENTAGE_5B,
												BloomreachConstants.SQUARE_BRACKET_OPEN)
										.replaceAll(BloomreachConstants.PERCENTAGE_5D,
												BloomreachConstants.SQUARE_BRACKET_CLOSE);
						refURL = selRefinements
								.replaceAll(BloomreachConstants.PERCENTAGE_20, BloomreachConstants.STRING_PLUS)
								.replaceAll(BloomreachConstants.PERCENTAGE_28, BloomreachConstants.OPEN_BRACKET)
								.replaceAll(BloomreachConstants.PERCENTAGE_29, BloomreachConstants.CLOSE_BRACKER)
								.replaceAll(BloomreachConstants.PLUS_WITH_BRACKET,
										BloomreachConstants.PERCENTAGE_WITH_BRACKET)
								.replace(selectedRefinement, BloomreachConstants.EMPTY_STRING);
						urlKidsType = queryParams.getProperty(BloomreachConstants.URL_KIDS_TYPE);
						if (null != urlKidsType && isExclude && null != urlQuery) {
							refURL = urlQuery.replaceAll(SearchConstants.SLASH.concat(urlKidsType),
									BloomreachConstants.EMPTY_STRING);
						} else if (isExclude && pString.equalsIgnoreCase(BloomreachConstants.AGE_GROUP)) {
							String test = refinmentSection.concat(text).replaceAll(SearchConstants.SPACE,
									BloomreachConstants.PERCENTAGE_20);
							refURL = selRefinements.replace(test, BloomreachConstants.EMPTY_STRING);
						}
					}
				}

				if (rrConfiguration.isEnableRunningShoesInWalkingCategory()
						&& null != queryParams.getProperty(SearchConstants.URL_QUERY)
						&& queryParams.getProperty(SearchConstants.URL_QUERY)
								.contains(BloomreachConstants.WALKING_STRING)
						&& text.equalsIgnoreCase(SearchConstants.RUNNING)) {
					continue;
				}
				text = findRefinementTitle(navigationMap.get(pString), text);

				refinementBean = new BloomreachSearchRefinementsDTO();
				refinementBean.setProducts(count);
				refinementBean.setName(text);
				if (disabledRef || null != searchRefinementQuery) {
					refinementBean.setState(BloomreachConstants.ACTIVE);
				} else {
					refinementBean.setState(BloomreachConstants.INACTIVE);
				}

				url = BloomreachConstants.URL_START_DELIMETER + refURL;
				String smartZone = queryParams.getProperty(BloomreachConstants.QPARAMS.SZ);
				try {
					if (null != queryParams.getProperty(BloomreachConstants.KDSBNR)
							&& !queryParams.getProperty(BloomreachConstants.KDSBNR).isEmpty()
							&& queryParams.getProperty(BloomreachConstants.KDSBNR).equals(SearchConstants.TRUE)) {
						url = url + SearchConstants.URL_DELIMETER2 + BloomreachConstants.KDSBNR
								+ BloomreachConstants.EQUAL + SearchConstants.TRUE;

					}
				} catch (NullPointerException ex) {
					log.debug("BloomreachRefinementSearchResults :: kids banner param is null :: ex ={}", ex);
				}
				if (refinementBean.getState().equals(BloomreachConstants.ACTIVE) && urlKidsType == null) {
					StringBuffer sb = new StringBuffer(url);
					if (null != seoFilterMap.get(navigationMap.get(pString))) {
						int len = seoFilterMap.get(navigationMap.get(pString)).length();
						if (len > 0 && url.length() > 0 && url.length() - len >= 0) {
							sb.delete(url.length() - len, url.length());
						}
					}
					url = sb.toString();
				}

				if (!StringUtils.isEmpty(queryParams.getProperty(BloomreachConstants.QPARAMS.P))) {
					url = url + SearchConstants.URL_DELIMETER2 + BloomreachConstants.QPARAMS.P
							+ BloomreachConstants.EQUAL + queryParams.getProperty(BloomreachConstants.QPARAMS.P);
				}
				if (!StringUtils.isEmpty(queryParams.getProperty(BloomreachConstants.QPARAMS.S))) {
					url = url + SearchConstants.URL_DELIMETER2 + BloomreachConstants.QPARAMS.S
							+ BloomreachConstants.EQUAL + queryParams.getProperty(BloomreachConstants.QPARAMS.S);
				}

				String scriParam = queryParams.getProperty(BloomreachConstants.BR_SEO_CATEGORY_ITEM);
				if (!StringUtils.isEmpty(scriParam)) {
					url += SearchConstants.URL_DELIMETER2 + BloomreachConstants.BR_SEO_CATEGORY_ITEM
							+ BloomreachConstants.EQUAL + scriParam;
				}
				refinementBean.setUrl(url.replaceAll(BloomreachConstants.PERCENTAGE_20, BloomreachConstants.PLUS)
						.replace(BloomreachConstants.PARAMETER_R_WITH2C, BloomreachConstants.R_EQUAL));
				log.debug("BloomreachRefinementSearchResults :: buildRefinementDetails() refinments url {}", url);
				if (null != searchRefinementQuery) {
					searchRefinementQuery = searchRefinementQuery.replaceAll(SearchConstants.SPACE_REGEX,
							SearchConstants.SPACE);
					String rParam = queryParams.getProperty(BloomreachConstants.QPARAMS.R);
					rParam = null != rParam ? BloomreachConstants.R_EQUAL.concat(rParam)
							: BloomreachConstants.EMPTY_STRING;
					refinementBean
							.setUrl(SearchConstants.SEARCH_CONTEXT_PATH.concat(searchRefinementQuery).concat(rParam));
				}
				refs.add(refinementBean);
				log.debug("BloomreachRefinementSearchResults :: buildRefinementDetails() END refinementBean {0}",
						refinementBean);
			}

		}
	}

	private String populatePriceRange(String start, String end) {
		String refUrl = BloomreachConstants.SQUARE_BRACKET_OPEN.concat(start).concat(SearchConstants.SPACE)
				.concat(BloomreachConstants.TO_STRING).concat(SearchConstants.SPACE).concat(end)
				.concat(BloomreachConstants.SQUARE_BRACKET_CLOSE);
		return refUrl;
	}

	private String isSearchDefaultRefinement(String pSearchQuery, String pText, Optional<String> pSearchColorName,
			boolean pIsSearchBrand, boolean pIsSearchGender, boolean isShoeSize, boolean isApparelSize,
			Properties queryParams) {
		String searchQuery = null;
		if (!StringUtils.isEmpty(pText) && null != pSearchColorName && pSearchColorName.isPresent()
				&& pText.equalsIgnoreCase(pSearchColorName.get())) {
			pSearchQuery = pSearchQuery.replace(pSearchColorName.get().toLowerCase(), BloomreachConstants.EMPTY_STRING);
			searchQuery = pSearchQuery.trim();
		} else if (!StringUtils.isEmpty(pText) && (pIsSearchGender)) {
			pSearchQuery = pSearchQuery.replace(
					pText.toLowerCase().replace(BloomreachConstants.SINGLE_QUOTES, BloomreachConstants.EMPTY_STRING),
					BloomreachConstants.EMPTY_STRING);
			searchQuery = pSearchQuery.trim();
		} else if (!StringUtils.isEmpty(pText) && (pIsSearchBrand)) {
			pSearchQuery = pSearchQuery.replace(pText.toLowerCase(), BloomreachConstants.EMPTY_STRING);
			searchQuery = pSearchQuery.trim();
		}
		if (!StringUtils.isEmpty(pText) && (isShoeSize)) {
			pSearchQuery = pSearchQuery.replace(SearchConstants.SIZE_WITH_SPACE.concat(pText.toLowerCase()),
					BloomreachConstants.EMPTY_STRING);
			searchQuery = pSearchQuery.trim();
		}
		if (!StringUtils.isEmpty(pText) && (isApparelSize)) {
			String searchApparelSize = BloomreachConstants.REMOVE_SIZE_REGEX_1
					.concat(queryParams.getProperty(BloomreachConstants.APPAREL_SIZE))
					.concat(BloomreachConstants.REMOVE_SIZE_REGEX_2);
			pSearchQuery = pSearchQuery.replaceAll(searchApparelSize, SearchConstants.SPACE);
			searchQuery = pSearchQuery.trim();
		}
		return searchQuery;
	}

	public String findRefinementTitle(String refName, String refValue) {
		log.debug("BloomreachRefinementSearchResults :: findRefinementTitle :: refName: {} refValue: {}", refName,
				refValue);
		String res = refValue;
		if (StringUtils.isEmpty(refName) || StringUtils.isEmpty(refValue)) {
			return res;
		}

		String refsDetail = refinementTitles.get(refName);
		if (StringUtils.isEmpty(refsDetail)) {
			return res;
		}

		String[] refsDetails = refsDetail.split("\\|");
		if (!(refsDetails.length <= 0 || refsDetails == null)) {
			for (String refDetail : refsDetails) {
				String[] refDetails = refDetail.split(SearchConstants.COLON);
				if (refDetails != null && refDetails.length == 2) {
					if (refValue.equals(refDetails[0])) {
						res = refDetails[1];
						break;
					}
				}
			}
		}
		log.debug("BloomreachRefinementSearchResults :: findRefinementTitle :: res: {}", res);
		return res;
	}
}
