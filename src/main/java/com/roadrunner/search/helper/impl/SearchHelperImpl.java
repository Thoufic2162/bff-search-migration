package com.roadrunner.search.helper.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.gson.Gson;
import com.roadrunner.search.config.BloomreachConfiguration;
import com.roadrunner.search.config.RRConfiguration;
import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.dto.BloomreachSearchResponseDTO;
import com.roadrunner.search.dto.CatalogElementsFinder;
import com.roadrunner.search.helper.SearchHelper;
import com.roadrunner.search.service.BloomreachSearchService;
import com.roadrunner.search.util.URLCoderUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@ConfigurationProperties(prefix = "searchhelper")
@Getter
@Setter
@SuppressWarnings("deprecation")
public class SearchHelperImpl implements SearchHelper {

	@Autowired
	private CatalogElementsFinder catalogElementsFinder;

	@Autowired
	private BloomreachSearchService bloomreachSearchService;

	@Autowired
	private BloomreachConfiguration bloomreachConfiguration;

	@Autowired
	private RRConfiguration rrConfiguration;

	@Autowired
	private Gson gson;

	private Map<String, String> sortOptionsMap;
	private Map<String, String> bloomreachUrlMap;

	@Override
	public String getBrSearchToCategoryUrl(String query) {
		boolean isCategory = false;
		boolean isSubCategory = false;
		boolean isGender = false;
		boolean isOutlet = false;
		boolean isBasement = false;
		boolean isBrand = false;
		boolean isColor = false;
		boolean isKids = false;
		boolean isKidsAccessories = false;
		boolean isValidSearch = false;
		String url = null;
		String[] urlParams = query.replaceAll(SearchConstants.APOSTROPHE, SearchConstants.EMPTY_STRING).toLowerCase()
				.split(SearchConstants.SPACE);
		String temp = query.replaceAll(SearchConstants.APOSTROPHE, SearchConstants.EMPTY_STRING).toLowerCase();
		List<String> queries = new LinkedList<String>();
		for (String param : urlParams) {
			String temporaryParam = SearchConstants.EMPTY_STRING;
			if (catalogElementsFinder.getGenderCategoryMap().containsKey(param)) {
				temporaryParam = param;
				param = catalogElementsFinder.getGenderCategoryMap().get(param);
				temp = temp.replace(temporaryParam, param);
			}
			if (catalogElementsFinder.getApparelSearchToCategoryMap().containsKey(param)) {
				temporaryParam = param;
				param = catalogElementsFinder.getApparelSearchToCategoryMap().get(param);
				temp = temp.replace(temporaryParam, param);
			}
			if (catalogElementsFinder.getAccessoriesSubPgc().containsKey(param)) {
				temporaryParam = param;
				param = catalogElementsFinder.getAccessoriesSubPgc().get(param);
				temp = temp.replace(temporaryParam, param);
			}
			if (catalogElementsFinder.getBrands().contains(param.toLowerCase())) {
				isBrand = true;
			}
			if (catalogElementsFinder.getColors().contains(param.toLowerCase())) {
				isColor = true;
			}
			if (catalogElementsFinder.getGender().contains(param)) {
				temp = temp.replaceAll(param, SearchConstants.EMPTY_STRING);
				queries.add(param);
				isGender = true;
				continue;
			}
			if (catalogElementsFinder.getPgcCategoryMap().containsKey(param)) {
				temporaryParam = param;
				param = catalogElementsFinder.getPgcCategoryMap().get(param);
				temp = temp.replace(temporaryParam, param);
			}
			if (catalogElementsFinder.getKidsGender().contains(param)) {
				temp = temp.replaceAll(param, SearchConstants.EMPTY_STRING);
				queries.add(param);
				isGender = true;
				isKids = true;
			}
			if (catalogElementsFinder.getCategoriesWithGear().contains(param)) {
				temp = temp.replaceAll(param, SearchConstants.EMPTY_STRING);
				queries.add(param);
				isCategory = true;
				continue;
			}
			if (param.equalsIgnoreCase(SearchConstants.PARAM_OUTLET)) {
				temp = temp.replaceAll(param, SearchConstants.EMPTY_STRING);
				queries.add(param);
				isOutlet = true;
				continue;
			}
			if (param.equalsIgnoreCase(SearchConstants.BASEMENT)) {
				temp = temp.replaceAll(param, SearchConstants.EMPTY_STRING);
				queries.add(param);
				isBasement = true;
				continue;
			}
			if (catalogElementsFinder.getKidsAccessoriesList().contains(param)) {
				isKidsAccessories = true;
			}
		}

		temp = temp.trim().replaceAll(SearchConstants.SPACE, BloomreachConstants.HYPHEN)
				.replaceAll(SearchConstants.REGEX_COMMA, SearchConstants.EMPTY_STRING);
		if (catalogElementsFinder.getSubCategory().contains(temp)) {
			queries.add(temp);
			isValidSearch = true;
			isSubCategory = true;
		} else if (!isGender && catalogElementsFinder.getApparelSearchToCategoryMap().containsKey(temp)) {
			queries.add(catalogElementsFinder.getApparelSearchToCategoryMap().get(temp));
			isValidSearch = true;
			isSubCategory = true;
		} else if (!isGender && catalogElementsFinder.getShoesSearchToCategoryMap().containsKey(temp)) {
			queries.add(catalogElementsFinder.getShoesSearchToCategoryMap().get(temp));
			isValidSearch = true;
			isSubCategory = true;
		}

		boolean outletSocks = (isOutlet && queries.contains(SearchConstants.SOCKS));

		if (!isCategory && (catalogElementsFinder.getShoesSearchToCategoryMap().containsKey(temp))) {
			queries.add(SearchConstants.SHOES);
			isCategory = true;
			isValidSearch = true;
		}
		if (!isCategory && catalogElementsFinder.getAccessoriesSubPgc().containsKey(temp)
				&& ((!outletSocks && !isKids) || isKidsAccessories)) {
			queries.add(SearchConstants.ACCESSORIES);
			isCategory = true;
			isValidSearch = true;
		}
		if (!isCategory && catalogElementsFinder.getApparelSearchToCategoryMap().containsKey(temp) && !isKids
				&& !outletSocks) {
			queries.add(SearchConstants.APPARELURL);
			isCategory = true;

		}
		if (!isCategory && catalogElementsFinder.getBoysSearchToCategoryMap().containsKey(temp) && !outletSocks
				&& isKids) {
			queries.add(SearchConstants.APPARELURL);
			isCategory = true;

		}
		if (!isCategory && catalogElementsFinder.getGirlsSearchToCategoryMap().containsKey(temp) && !outletSocks
				&& isKids) {
			queries.add(SearchConstants.APPARELURL);
			isCategory = true;

		}
		if (!isCategory && catalogElementsFinder.getEquipmentSearchToCategoryMap().containsKey(temp)) {
			queries.add(SearchConstants.EQUIPMENT);
			isCategory = true;
			isValidSearch = true;
		}
		if (!isCategory && catalogElementsFinder.getNutritionSearchToCategoryList().contains(temp)) {
			queries.add(SearchConstants.NUTRITION);
			isCategory = true;
			isValidSearch = true;
		}
		if (!isCategory && catalogElementsFinder.getPersonalCareSearchToCategoryMap().containsKey(temp)) {
			queries.add(SearchConstants.PERSONAL_CARE);
			isCategory = true;
			isValidSearch = true;
		}

		if (!isSubCategory) {
			if ((queries.contains(SearchConstants.WOMENS)
					&& catalogElementsFinder.getWomensApparelSubCategoryMap().containsKey(temp))) {
				queries.add(catalogElementsFinder.getWomensApparelSubCategoryMap().get(temp));
				isSubCategory = true;
				isValidSearch = true;
			} else if ((queries.contains(SearchConstants.MENS)
					&& catalogElementsFinder.getMensApparelSubCategoryMap().containsKey(temp))) {
				queries.add(catalogElementsFinder.getMensApparelSubCategoryMap().get(temp));
				isSubCategory = true;
				isValidSearch = true;
			}
			if ((queries.contains(SearchConstants.WOMENS) || queries.contains(SearchConstants.MENS))
					&& queries.contains(SearchConstants.SHOES)
					&& catalogElementsFinder.getShoesSearchToCategoryMap().containsKey(temp)) {

				queries.add(catalogElementsFinder.getShoesSearchToCategoryMap().get(temp));
				isSubCategory = true;
				isValidSearch = true;

			}
			if (queries.contains(SearchConstants.BOYS)
					&& catalogElementsFinder.getBoysSearchToCategoryMap().containsKey(temp)) {
				queries.add(catalogElementsFinder.getBoysSearchToCategoryMap().get(temp));
				isSubCategory = true;
				isValidSearch = true;

			}
			if (queries.contains(SearchConstants.GIRLS)
					&& catalogElementsFinder.getGirlsSearchToCategoryMap().containsKey(temp)) {
				queries.add(catalogElementsFinder.getGirlsSearchToCategoryMap().get(temp));
				isSubCategory = true;
				isValidSearch = true;
			}
			if (catalogElementsFinder.getEquipmentSearchToCategoryMap().containsKey(temp)) {
				queries.add(catalogElementsFinder.getEquipmentSearchToCategoryMap().get(temp));
				isSubCategory = true;
				isValidSearch = true;

			}
			if (catalogElementsFinder.getPersonalCareSearchToCategoryMap().containsKey(temp)) {
				queries.add(catalogElementsFinder.getPersonalCareSearchToCategoryMap().get(temp));
				isSubCategory = true;
				isValidSearch = true;
			}

			if (catalogElementsFinder.getApparelSearchToCategoryMap().containsKey(temp) && !isGender) {
				queries.add(catalogElementsFinder.getApparelSearchToCategoryMap().get(temp));
				isSubCategory = true;
				isValidSearch = true;
			}

			if (catalogElementsFinder.getAccessoriesSubPgc().containsKey(temp) && !isGender
					&& !queries.contains(SearchConstants.SHOES)) {
				queries.add(catalogElementsFinder.getAccessoriesSubPgc().get(temp));
				isSubCategory = true;
				isValidSearch = true;
			}

		}

		StringBuffer sb = new StringBuffer();
		if (isValidSearch) {
			temp = temp.replaceAll(temp, SearchConstants.EMPTY_STRING);
		}
		boolean isColorAndBrandSearch = !isColor && !isBrand;
		boolean isExactSearch = isGender && isCategory && isSubCategory && queries.size() == 3;
		boolean isGenderSearch = isGender && queries.size() == 1;
		boolean isGenderAndCategorySearch = isGender && isCategory && queries.size() == 2;
		boolean isOutletProductsSearch = ((isOutlet && isGender && isCategory) || (isOutlet && isSubCategory)
				|| (isBasement && isCategory) || (isOutlet && isBasement));
		boolean isGenderAndSubCategorySearch = isGender && isSubCategory;
		boolean isOutletCategorySearch = isOutlet && isCategory;
		boolean isOutletAndGenderSearch = isOutlet && isGender;
		boolean isCategoryOrSubCategorySearch = (isCategory || isSubCategory)
				&& (queries.size() == 2 || queries.size() == 1);
		boolean isValid = !outletSocks && isColorAndBrandSearch
				&& (isExactSearch || isGenderSearch || isGenderAndCategorySearch || isOutletProductsSearch
						|| isGenderAndSubCategorySearch || isOutletCategorySearch || isOutletAndGenderSearch
						|| isCategoryOrSubCategorySearch);

		String queryString = SearchConstants.EMPTY_STRING;
		int querylength = urlParams.length;
		for (String queryValues : urlParams) {
			if (querylength != 1) {
				queryValues = queryValues.concat(SearchConstants.PLUS);
			}
			queryString = queryString.concat(queryValues.toLowerCase());
			--querylength;
		}

		if (StringUtils.isNotEmpty(queryString) && catalogElementsFinder.getSearchQueryMap().containsKey(queryString)) {
			url = catalogElementsFinder.getSearchQueryMap().get(queryString);
		} else if (StringUtils.isNotEmpty(temp)) {
			return url;
		} else if (isValid) {
			for (String item : queries) {
				sb.append(item);
				sb.append(SearchConstants.SLASH);
			}
			url = reOrderUrl(sb, isGender, isCategory, isSubCategory);
		}
		return url;
	}

	private String reOrderUrl(StringBuffer url, boolean isGender, boolean isCategory, boolean isSubCategory) {
		boolean isKids = false;
		boolean isOutlet = false;
		boolean isOutletCategory = false;
		boolean isOutletSubCategory = false;
		boolean isOutletExactSearch = isOutlet && isOutletCategory && isGender && isOutletSubCategory;
		boolean isOutletAndGeneralSearch = ((isOutlet && isOutletCategory) && !isGender)
				|| (isCategory && isSubCategory);

		String[] urls = url.toString().split(SearchConstants.SLASH);
		Map<String, Integer> urlmap = new HashMap<String, Integer>();
		for (String aUrl : urls) {
			if (aUrl.equalsIgnoreCase(SearchConstants.BOYS) || aUrl.equalsIgnoreCase(SearchConstants.GIRLS)
					|| aUrl.equalsIgnoreCase(SearchConstants.KIDS)) {
				isKids = true;
			}
			if (catalogElementsFinder.getOutletSubCategoryList().contains(aUrl)) {
				isOutletSubCategory = true;
			}
			if (aUrl.equalsIgnoreCase(SearchConstants.OUTLET)) {
				isOutlet = true;
			}
			if (catalogElementsFinder.getCategory().contains(aUrl)) {
				isOutletCategory = true;
			}
			if (catalogElementsFinder.getGender().contains(aUrl)) {
				urlmap.put(aUrl, 1);
				continue;
			}
			if (catalogElementsFinder.getCategoriesWithGear().contains(aUrl)) {
				urlmap.put(aUrl, isOutletAndGeneralSearch ? 2 : 3);
				continue;
			}
			if (catalogElementsFinder.getSubCategory().contains(aUrl)) {
				urlmap.put(aUrl, isOutletExactSearch ? 5 : 4);
				continue;
			}
			if (aUrl.contains(SearchConstants.PARAM_OUTLET)) {
				urlmap.put(aUrl, 6);
				continue;
			}
			if (aUrl.contains(SearchConstants.BASEMENT)) {
				urlmap.put(aUrl, 7);
				continue;
			}

		}
		List<String> sortedurl = urlmap.entrySet().stream().sorted(Entry.comparingByValue()).map(Map.Entry::getKey)
				.collect(Collectors.toList());

		url.setLength(0);
		if (isKids) {
			url.append(SearchConstants.CATEGORY_KIDS);
		} else {
			url.append(SearchConstants.CATEGORY_URL);
		}
		url.append(String.join(SearchConstants.SLASH, sortedurl));
		return url.toString();
	}

	@Override
	public void populateClearRefUrl(HttpServletRequest request, String query, Optional<String> color,
			Optional<String> searchBrand, Optional<String> searchGender, String searchShoeSize,
			Optional<String> searchApparelSize) {
		if (null != color && color.isPresent()) {
			String[] colorArray = color.get().split(SearchConstants.COMMA);
			query = null != colorArray[0] ? query.replace(colorArray[0], BloomreachConstants.EMPTY_STRING) : query;
		}
		if (null != searchBrand && searchBrand.isPresent()) {
			query = query.replace(searchBrand.get(), BloomreachConstants.EMPTY_STRING);
		}
		if (null != searchGender && searchGender.isPresent()) {
			query = query.replace(searchGender.get(), BloomreachConstants.EMPTY_STRING);
		}
		if (null != searchShoeSize) {
			query = query.replace(searchShoeSize, BloomreachConstants.EMPTY_STRING);
		}
		if (null != searchApparelSize && searchApparelSize.isPresent()) {
			query = query.replace(searchApparelSize.get(), BloomreachConstants.EMPTY_STRING);
		}
		String searchClearFilterUrl = query
				.replaceAll(SearchConstants.SEARCH_CONTEXT_PATH, BloomreachConstants.EMPTY_STRING)
				.replaceAll(SearchConstants.SPACE_REGEX, SearchConstants.SPACE).trim();
		if (StringUtils.isEmpty(searchClearFilterUrl)) {
			request.setAttribute(BloomreachConstants.CLEAR_FILTER_URL,
					SearchConstants.SEARCH_CONTEXT_PATH.concat(SearchConstants.STAR));
		} else {
			request.setAttribute(BloomreachConstants.CLEAR_FILTER_URL,
					SearchConstants.SEARCH_CONTEXT_PATH.concat(searchClearFilterUrl));
		}
	}

	@Override
	public BloomreachSearchResponseDTO performSearch(Properties queryParams, HttpServletRequest request,
			boolean isSearch) {
		log.debug("SearchHelperImpl:: performSearch() :: queryParams= {}, isSearch= {}", queryParams, isSearch);
		BloomreachSearchResponseDTO bloomreachSearchResponseDTO = new BloomreachSearchResponseDTO();
		String qParam = null;
		String smartZone = queryParams.getProperty(BloomreachConstants.QPARAMS.SZ);
		String searchQuery = URLCoderUtil.decode(queryParams.getProperty(BloomreachConstants.QPARAMS.QUERY));
		String url;
		String responseJson = null;
		String count = queryParams.getProperty(SearchConstants.QUERY_COUNT);
		String isCustomQuery = queryParams.getProperty(SearchConstants.IS_CUSTOMQUERY);
		String kidsGender = SearchConstants.EMPTY_STRING;
		String query = URLCoderUtil.decode(request.getParameter(SearchConstants.QURI));
		if (null != queryParams.getProperty(BloomreachConstants.SEARCH_REDIRECT_URL)) {
			query = queryParams.getProperty(SearchConstants.URL_QUERY);
		}
		if (count != null && count.equalsIgnoreCase(SearchConstants.TRUE)) {
			return null;
		}
		if (isCustomQuery != null && isCustomQuery.equalsIgnoreCase(SearchConstants.FALSE)) {
			return null;
		}
		if (query.contains(SearchConstants.SITE_KIDS)) {
			kidsGender = BloomreachConstants.URL_DELIMETER + BloomreachConstants.KIDS_GENDER_TEXT;
		}
		if (StringUtils.isNotBlank(queryParams.getProperty(BloomreachConstants.QPARAMS.QUERY))) {
			isSearch = true;
		}
		try {
			if (isSearch && searchQuery != null) {
				boolean szb = SearchConstants.TRUE.equalsIgnoreCase(smartZone);
				String outOfStockQuery = BloomreachConstants.EMPTY_STRING;
				log.debug("SearchHelper:: performSearch() :: smartZone= {}, szb= {}", smartZone, szb);
				if (szb) {
					searchQuery = URLCoderUtil.decode(searchQuery) + BloomreachConstants.URL_DELIMETER
							+ BloomreachConstants.QPARAMS.SZ_PARAM_STR + smartZone;
					searchQuery = URLCoderUtil.encode(searchQuery);
				}
				if (szb) {
					outOfStockQuery = BloomreachConstants.URL_DELIMETER
							.concat(BloomreachConstants.ENABLE_IN_PRODUCT_CUSTOM_SEARCH);
				}
				if (searchQuery.equalsIgnoreCase(SearchConstants.ONE_BY_OPTIC_NERVE_WITH_HYPHEN)
						|| searchQuery.equalsIgnoreCase(SearchConstants.ONE_BY_OPTICNERVE)) {
					searchQuery = SearchConstants.OPTIC_NERVE;
				}
				qParam = BloomreachConstants.PARAMETER_Q.concat(searchQuery);
				if (null != queryParams.get(BloomreachConstants.SHOE_SIZE_PARAM)) {
					qParam = BloomreachConstants.PARAMETER_Q
							.concat(searchQuery.replace((String) queryParams.get(BloomreachConstants.SHOE_SIZE_PARAM),
									SearchConstants.EMPTY_STRING));
				}
				if (null != queryParams.get(BloomreachConstants.APPAREL_SIZE)) {
					qParam = BloomreachConstants.PARAMETER_Q.concat(searchQuery.replace(
							(String) queryParams.get(BloomreachConstants.APPAREL_SIZE), SearchConstants.EMPTY_STRING));
				}
				qParam = qParam.replaceAll(SearchConstants.SPACE, BloomreachConstants.PERCENTAGE_20);
				url = MessageFormat.format(bloomreachConfiguration.getSearchApiUrl(),
						getParamsString(populateRequestParam(queryParams), isSearch, queryParams)
								+ BloomreachConstants.URL_DELIMETER + qParam.concat(outOfStockQuery));
			} else {
				url = MessageFormat.format(bloomreachConfiguration.getSearchApiUrl(),
						getParamsString(populateRequestParam(queryParams), isSearch, queryParams)) + kidsGender;
			}
			responseJson = bloomreachSearchService.bloomreachSearchApiCall(url);
			log.debug("SearchHelper.performSearch: bloomreachSearchUrl{}", url);
			if (null != responseJson) {
				bloomreachSearchResponseDTO = gson.fromJson(responseJson, BloomreachSearchResponseDTO.class);
			}
		} catch (IOException ioException) {
			log.error("SearchHelperImpl::performSearch::ioException={}", ioException);
		}
		log.debug("SearchHelperImpl::performSearch:: END");
		return bloomreachSearchResponseDTO;
	}

	public Map<String, String> populateRequestParam(Properties queryParams) {
		log.debug("SearchHelperImpl.populateRequestParam: START :: queryParams={}", queryParams);
		StringBuffer host = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
				.getRequestURL();
		String domain = null;
		boolean isSort = false;
		try {
			URL url = new URL(host.toString());
			domain = url.getProtocol() + BloomreachConstants.URL_SLASH + url.getHost();
		} catch (MalformedURLException malformedURLException) {
			log.error("SearchHelperImpl::populateRequestParam malformedURLException={}", malformedURLException);
		}
		String skip = queryParams.getProperty(BloomreachConstants.QPARAMS.SKIP);
		String uid = queryParams.getProperty(BloomreachConstants.UID);
		String sort = null;
		if (!StringUtils.isEmpty(queryParams.getProperty(BloomreachConstants.QPARAMS.S))) {
			sort = queryParams.getProperty(BloomreachConstants.QPARAMS.S);
		}
		if (rrConfiguration.isEnableProductRanking() && sort.equalsIgnoreCase(BloomreachConstants.START_COUNT)) {
			isSort = true;
		}
		Map<String, String> constructParam = new HashMap<>();
		constructParam.put(BloomreachConstants.ACCOUNT_ID, getBloomreachConfiguration().getAccountId());
		constructParam.put(BloomreachConstants.AUTH_KEY, getBloomreachConfiguration().getAuthKey());
		constructParam.put(BloomreachConstants.DOMAIN_KEY, getBloomreachConfiguration().getDomainKey());
		constructParam.put(BloomreachConstants.FL,
				String.join(BloomreachConstants.COMMA, getBloomreachConfiguration().getFl()));
		constructParam.put(BloomreachConstants.URL, domain);
		constructParam.put(BloomreachConstants.REF_URL, domain);
		if (queryParams.get(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR) != null
				&& queryParams.get(SearchConstants.TYPE_KEYWORD) == null) {
			constructParam.put(BloomreachConstants.SEARCH_TYPE, getBloomreachConfiguration().getCatagorytype());
		} else {
			constructParam.put(BloomreachConstants.SEARCH_TYPE, getBloomreachConfiguration().getSearchType());
		}
		constructParam.put(BloomreachConstants.REQUEST_TYPE, getBloomreachConfiguration().getRequestType());
		constructParam.put(BloomreachConstants.ROWS, BloomreachConstants.ROWS_COUNT);
		if (null != uid) {
			constructParam.put(BloomreachConstants.UID_PARAM, uid);
		}
		if (null != sort && !isSort && null != getSortOptionsMap().get(sort)) {

			constructParam.put(BloomreachConstants.SORT, getSortOptionsMap().get(sort)
					.replace(BloomreachConstants.URL_DELIMETER, BloomreachConstants.COMMA));
		}
		if (null != skip) {
			constructParam.put(BloomreachConstants.START, skip);
		} else {
			constructParam.put(BloomreachConstants.START, BloomreachConstants.START_COUNT);
		}
		log.debug("SearchHelperImpl::populateRequestParam: END constructParam {}", constructParam);
		return constructParam;

	}

	public String getParamsString(Map<String, String> queryParams, boolean isSearch, Properties pQueryParams)
			throws IOException {
		log.debug("SearchHelperImpl.getParamsString: START:: queryParams={}, pQueryParams={}, ", queryParams,
				pQueryParams);
		String resultString = null;
		String pRParams = null;
		String urlQuery = null;
		String smartZone = pQueryParams.getProperty(BloomreachConstants.QPARAMS.SZ);
		boolean szb = null != smartZone && SearchConstants.TRUE.equalsIgnoreCase(smartZone);
		if (pQueryParams != null && pQueryParams.get(BloomreachConstants.QPARAMS.R) != null) {
			pRParams = pQueryParams.get(BloomreachConstants.QPARAMS.R).toString();
		}
		if (pQueryParams != null && pQueryParams.get(SearchConstants.URL_QUERY) != null) {
			urlQuery = pQueryParams.get(SearchConstants.URL_QUERY).toString();
		}
		if (null != queryParams) {
			StringBuilder result = new StringBuilder();
			for (Map.Entry<String, String> entry : queryParams.entrySet()) {
				String value = entry.getValue();
				result.append(URLEncoder.encode(entry.getKey(), BloomreachConstants.UTF_8));
				result.append(BloomreachConstants.EQUAL);
				if (!entry.getKey().equals(BloomreachConstants.SORT)) {
					value = URLEncoder.encode(entry.getValue(), BloomreachConstants.UTF_8);
				}
				result.append(value);
				result.append(BloomreachConstants.URL_DELIMETER);
			}
			if (!isSearch && null != pRParams) {
				pRParams = pRParams.replaceAll(BloomreachConstants.URL_DELIMETER, BloomreachConstants.PERCENTAGE_26)
						.replaceAll(SearchConstants.SPACE, BloomreachConstants.PERCENTAGE_20);
				if (rrConfiguration.isEnableRunningShoesInWalkingCategory() && null != urlQuery
						&& urlQuery.contains(BloomreachConstants.WALKING_STRING)) {
					pRParams = pRParams.concat(BloomreachConstants.COMMA.concat(BloomreachConstants.CAT_RUNNING));
				}
				for (Map.Entry<String, String> entry : getBloomreachUrlMap().entrySet()) {
					pRParams = pRParams.replaceAll(entry.getKey(), entry.getValue());
				}
				result.append(BloomreachConstants.PARAMETER_FQ);
				result.append(pRParams.replaceAll(SearchConstants.COLON, BloomreachConstants.COLON_WITH_QUOTES)
						.replaceAll(BloomreachConstants.COMMA, BloomreachConstants.QUOTES_WITH_FQ));
				result.append(BloomreachConstants.QUOTES);
				String categoryParam = BloomreachConstants.EMPTY_STRING;
				if (null != pQueryParams.get(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR)) {
					categoryParam = (String) pQueryParams.get(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR);
				}
				if (!StringUtils.isEmpty(categoryParam)) {
					result.append(BloomreachConstants.PARAMETER_Q_WITHOUT_STAR
							+ pQueryParams.get(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR));
				} else if (null != pQueryParams.get(BloomreachConstants.IS_GENDER)) {
					result.append(BloomreachConstants.PARAMETER_Q_WITHOUT_STAR
							.concat((String) pQueryParams.get(BloomreachConstants.IS_GENDER)));
				} else {
					result.append(BloomreachConstants.PARAMETER_Q_WITH_STAR);
				}
			} else {
				if (null != pRParams) {
					pRParams = pRParams.replaceAll(BloomreachConstants.URL_DELIMETER, BloomreachConstants.PERCENTAGE_26)
							.replaceAll(SearchConstants.SPACE, BloomreachConstants.PERCENTAGE_20)
							.replaceAll(BloomreachConstants.PERCENTAGE_3A, SearchConstants.COLON)
							.replaceAll(BloomreachConstants.PERCENTAGE_2C, BloomreachConstants.COMMA);
					for (Map.Entry<String, String> entry : getBloomreachUrlMap().entrySet()) {
						pRParams = pRParams.replaceAll(entry.getKey(), entry.getValue());
					}
					result.append(BloomreachConstants.PARAMETER_FQ);
					result.append(pRParams.replaceAll(SearchConstants.COLON, BloomreachConstants.COLON_WITH_QUOTES)
							.replaceAll(BloomreachConstants.COMMA, BloomreachConstants.QUOTES_WITH_FQ));
					result.append(BloomreachConstants.QUOTES);
					if (pQueryParams.get(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR) != null) {
						result.append(BloomreachConstants.PARAMETER_Q_WITHOUT_STAR
								.concat((String) pQueryParams.get(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR)));
					}
				} else if (null != pQueryParams.get(BloomreachConstants.IS_GENDER)) {
					result.append(BloomreachConstants.PARAMETER_Q_WITHOUT_STAR
							.concat((String) pQueryParams.get(BloomreachConstants.IS_GENDER)));
				}
			}
			if (!szb) {
				result.append(BloomreachConstants.URL_DELIMETER.concat(BloomreachConstants.DISABLE_IN_PRODUCTSEARCH));
				formSearchColorFilters(pQueryParams, result);
			}
			resultString = result.toString();
		}
		log.debug("SearchHelperImpl.getParamsString: urlString{}", resultString);
		log.debug("SearchHelperImpl.getParamsString: END::");
		return resultString;
	}

	public void formSearchColorFilters(Properties pQueryParams, StringBuilder pResult) {
		if (pQueryParams.get(SearchConstants.SEARCH_COLOR) != null) {
			String[] colors = ((String) pQueryParams.get(SearchConstants.SEARCH_COLOR))
					.split(BloomreachConstants.COMMA);
			for (String color : colors) {
				pResult.append(BloomreachConstants.URL_DELIMETER);
				pResult.append(BloomreachConstants.PARAMETER_FQ);
				pResult.append(BloomreachConstants.COLOR_GROUP.concat(BloomreachConstants.QUOTES)
						.concat(WordUtils.capitalize(color)).concat(BloomreachConstants.QUOTES));
			}
		}
		if (pQueryParams.get(SearchConstants.SEARCH_BRAND) != null && getCatalogElementsFinder().getBloomreachBrandMap()
				.get(pQueryParams.get(SearchConstants.SEARCH_BRAND)) != null) {
			String brandName = (String) pQueryParams.get(SearchConstants.SEARCH_BRAND);
			pResult.append(BloomreachConstants.URL_DELIMETER);
			pResult.append(BloomreachConstants.PARAMETER_FQ);
			pResult.append(BloomreachConstants.BRAND_PARAMETER.concat(BloomreachConstants.QUOTES)
					.concat(getCatalogElementsFinder().getBloomreachBrandMap().get(brandName))
					.concat(BloomreachConstants.QUOTES));
			if (brandName.equalsIgnoreCase(SearchConstants.OPTIC_NERVE)
					|| brandName.equalsIgnoreCase(SearchConstants.ONE_BY_OPTIC_NERVE_WITH_HYPHEN)) {
				pResult.append(BloomreachConstants.URL_DELIMETER);
				pResult.append(BloomreachConstants.PARAMETER_FQ);
				pResult.append(BloomreachConstants.BRAND_PARAMETER.concat(BloomreachConstants.QUOTES)
						.concat(brandName.equalsIgnoreCase(SearchConstants.OPTIC_NERVE)
								? SearchConstants.ONE_BY_OPTIC_NERVE
								: SearchConstants.OPTICNERVE)
						.concat(BloomreachConstants.QUOTES));
			}
		}
		if (pQueryParams.get(SearchConstants.SEARCH_GENDER) != null) {
			String genderName = (String) pQueryParams.get(SearchConstants.SEARCH_GENDER);
			pResult.append(BloomreachConstants.URL_DELIMETER);
			pResult.append(BloomreachConstants.PARAMETER_FQ);
			if (genderName.equalsIgnoreCase(SearchConstants.KIDS)) {
				pResult.append(BloomreachConstants.KIDS_GENDER.concat(BloomreachConstants.QUOTES)
						.concat(WordUtils.capitalize(SearchConstants.KIDS)).concat(BloomreachConstants.QUOTES));
			} else {
				pResult.append(BloomreachConstants.GENDER_TEXT.concat(BloomreachConstants.QUOTES).concat(genderName)
						.concat(BloomreachConstants.QUOTES));
			}
		}
		if (pQueryParams.get(BloomreachConstants.SHOE_SIZE_PARAM) != null) {
			String shoeSize = (String) pQueryParams.get(BloomreachConstants.SHOE_SIZE_PARAM);
			pResult.append(BloomreachConstants.URL_DELIMETER);
			pResult.append(BloomreachConstants.SHOE_SIZE_WITH_FILTER
					.concat(shoeSize.replace(SearchConstants.SIZE_WITH_SPACE, BloomreachConstants.EMPTY_STRING))
					.concat(BloomreachConstants.QUOTES));
		}
		if (pQueryParams.get(BloomreachConstants.APPAREL_SIZE) != null) {
			String apparelSize = (String) pQueryParams.get(BloomreachConstants.APPAREL_SIZE);
			pResult.append(BloomreachConstants.URL_DELIMETER);
			pResult.append(BloomreachConstants.APPAREL_SIZE_WITH_FILTER
					.concat(getCatalogElementsFinder().getApparelSizeMap().get(apparelSize))
					.concat(BloomreachConstants.QUOTES));
		}
	}

}
