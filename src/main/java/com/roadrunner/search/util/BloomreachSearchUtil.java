package com.roadrunner.search.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.roadrunner.search.config.BloomreachConfiguration;
import com.roadrunner.search.config.RRConfiguration;
import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.domain.SeoContent;
import com.roadrunner.search.dto.CatalogElementsFinder;
import com.roadrunner.search.dto.CategoryItemDTO;
import com.roadrunner.search.helper.SearchHelper;
import com.roadrunner.search.repo.RRSCategoryMapRepository;
import com.roadrunner.search.repo.SeoContentRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@ConfigurationProperties(prefix = "bloomreachsearchutil")
@SuppressWarnings({ "deprecation", "unused" })
@Getter
@Setter
public class BloomreachSearchUtil {

	private List<String> searchGenders;
	private List<String> excludeDynamicurlList;
	private Map<String, String> urlOrderMap;
	private List<String> genderUrlList;
	private Map<String, String> urlMap;
	private List<String> excludedCategoryList;
	private List<String> excludedSubCategoryOrderList;
	private Map<String, String> sportsMap;
	private Map<String, String> sortOptionsMap;
	private Integer siteId;

	@Autowired
	private SearchHelper searchHelper;

	@Autowired
	private CatalogElementsFinder catalogElementsFinder;

	@Autowired
	private RRConfiguration rrConfiguration;

	@Autowired
	private SeoContentRepository seoContentRepository;

	@Autowired
	private BloomreachConfiguration bloomreachConfiguration;

	@Autowired
	private RRSCategoryMapRepository rrsCategoryMapRepository;

	/**
	 * This method is used to construct query parameters
	 * 
	 * @param request
	 */
	public void constructQueryParams(HttpServletRequest request) {
		String queryString = request.getQueryString();
		String query = URLCoderUtil.decode(request.getParameter(SearchConstants.QURI));
		String searchRedirectUrl = searchHelper.getBrSearchToCategoryUrl(
				query.replace(SearchConstants.SEARCH_CONTEXT_PATH, SearchConstants.EMPTY_STRING).toLowerCase());
		if (null != searchRedirectUrl) {
			query = searchRedirectUrl;
			request.setAttribute(BloomreachConstants.SEARCH_REDIRECT_URL, SearchConstants.TRUE);
		}
		if (null != query) {
			query = query.replace(SearchConstants.APOSTROPHE, SearchConstants.EMPTY_STRING).toLowerCase();
		}
		if (query != null && query.contains(SearchConstants.URL_DELIMETER2)
				&& !query.contains(SearchConstants.SEARCH)) {
			String[] params = query.split(SearchConstants.URL_DELIMETER2);
			if (params[0] != null) {
				request.setAttribute(SearchConstants.DECODEURI, params[0]);
			}
			Arrays.stream(params).filter(param -> param.contains(BloomreachConstants.EQUAL))
					.map(param -> param.split(BloomreachConstants.EQUAL)).forEach(urlParam -> {
						request.setAttribute(urlParam[0], urlParam[1]);
						request.setAttribute(SearchConstants.ISDECODE, SearchConstants.TRUE);
					});
		}
		Properties queryParams = HttpUtil.getRequestAttributesAndParameters(request);
		String scriParam = queryParams.getProperty(BloomreachConstants.BR_SEO_CATEGORY_ITEM);
		String cQuery = URLCoderUtil.decode((String) queryParams.getProperty(SearchConstants.COLOR));
		String catQuery = URLCoderUtil.decode((String) queryParams.getProperty(SearchConstants.CATEGORY_Q));
		String bQuery = URLCoderUtil.decode((String) queryParams.getProperty(SearchConstants.BRAND));
		String rQuery = URLCoderUtil.decode((String) queryParams.getProperty(SearchConstants.R));
		String shoeQuery = URLCoderUtil.decode((String) queryParams.getProperty(SearchConstants.SHOE_TYPE));
		String sportsQuery = URLCoderUtil.decode((String) queryParams.getProperty(SearchConstants.SPORTS_TYPE));
		if (StringUtils.isNotEmpty(queryParams.getProperty(SearchConstants.ISDECODE))) {
			if (StringUtils.isNotEmpty(queryParams.getProperty(SearchConstants.COLOR))) {
				cQuery = (String) queryParams.getProperty(SearchConstants.COLOR);
			}
			if (StringUtils.isNotEmpty(queryParams.getProperty(SearchConstants.CATEGORY_Q))) {
				catQuery = (String) queryParams.getProperty(SearchConstants.CATEGORY_Q);
			}
			if (StringUtils.isNotEmpty(queryParams.getProperty(SearchConstants.BRAND))) {
				bQuery = (String) queryParams.getProperty(SearchConstants.BRAND);
			}
			if (StringUtils.isNotEmpty(queryParams.getProperty(SearchConstants.SHOE_TYPE))) {
				shoeQuery = (String) queryParams.getProperty(SearchConstants.SHOE_TYPE);
			}
			if (StringUtils.isNotEmpty(queryParams.getProperty(SearchConstants.SPORTS_TYPE))) {
				sportsQuery = (String) queryParams.getProperty(SearchConstants.SPORTS_TYPE);
			}
			if (StringUtils.isNotEmpty(queryParams.getProperty(SearchConstants.DECODEURI))) {
				query = (String) queryParams.getProperty(SearchConstants.DECODEURI);
			}
			log.debug(
					"BloomreachSearchUtil:: constructQueryParams() ::cQuery::{}::catQuery::{}::bQuery::{}::query::{} shoeQuery::{} sportsQuery::{}",
					cQuery, catQuery, bQuery, query, shoeQuery, sportsQuery);
		}

		if (StringUtils.isNotBlank(queryString)) {
			String[] queryStringSplit = queryString.split(BloomreachConstants.EQUAL);
			if (null != (queryStringSplit) && queryStringSplit.length > 1
					&& SearchConstants.TYPE_AHEAD_SEARCH_QUERY_STRING.equalsIgnoreCase(queryStringSplit[0])) {
				query = SearchConstants.TYPE_AHEAD_SEARCH_QUERY + queryStringSplit[1];
			}
		}
		log.debug("BloomreachSearchUtil:: constructQueryParams():: queryParams{} queryString{} query{} scriParam {}",
				queryParams, queryString, query, scriParam);
		String requestURI = BloomreachConstants.EMPTY_STRING;
		if (StringUtils.isNotEmpty(query)) {
			if (query.contains(SearchConstants.SEARCH_CONTEXT_PATH)
					|| query.contains(SearchConstants.BRAND_TYPE_AHEAD_URL)) {
				query = query.replaceAll(SearchConstants.BRAND_TYPE_AHEAD_URL, SearchConstants.SEARCH_CONTEXT_PATH);
				String arr[] = query.split(SearchConstants.SEARCH_CONTEXT_PATH);
				if (arr != null && arr.length > 1) {
					request.setAttribute(BloomreachConstants.QPARAMS.QUERY, arr[1]);
					String uri = query;
					String selColor = rQuery;
					Optional<String> color = Arrays
							.stream(uri
									.replaceAll(SearchConstants.SEARCH_CONTEXT_PATH, BloomreachConstants.EMPTY_STRING)
									.split(SearchConstants.SPACE))
							.filter(isColor -> catalogElementsFinder.getColors().stream()
									.anyMatch(clr -> (isColor.equalsIgnoreCase(clr))))
							.findAny();
					Optional<String> refineSelcolor = catalogElementsFinder
							.getColors().stream().filter(
									clr -> null != selColor
											&& selColor
													.replaceAll(
															SearchConstants.VARIANTS_COLORGROUP
																	.concat(SearchConstants.COLON),
															SearchConstants.EMPTY_STRING)
													.toLowerCase().equalsIgnoreCase(clr))
							.findAny();
					Optional<String> searchBrand = Arrays
							.stream(uri
									.replaceAll(SearchConstants.SEARCH_CONTEXT_PATH, BloomreachConstants.EMPTY_STRING)
									.split(SearchConstants.SPACE))
							.filter(isBrand -> catalogElementsFinder.getBrands().stream()
									.anyMatch(brand -> isBrand.equalsIgnoreCase(brand)))
							.findAny();
					Optional<String> searchGender = searchGenders.stream().filter(gender -> uri.contains(gender))
							.findAny();
					Optional<String> searchApparelSize = Arrays
							.stream(uri
									.replaceAll(SearchConstants.SEARCH_CONTEXT_PATH, BloomreachConstants.EMPTY_STRING)
									.split(SearchConstants.SPACE))
							.filter(isapparelSize -> null != catalogElementsFinder.getApparelSizeMap()
									.get(isapparelSize))
							.findAny();

					String searchShoeSize = null;
					if (query.contains(SearchConstants.SIZE_LOWER_CASE)) {
						String sizePattern = SearchConstants.SIZE_PATTERN;
						Pattern regexPattern = Pattern.compile(sizePattern);
						Matcher matcher = regexPattern.matcher(query);

						if (matcher.find()) {
							searchShoeSize = SearchConstants.SIZE_WITH_SPACE.concat(matcher.group(1));
							request.setAttribute(BloomreachConstants.SHOE_SIZE_PARAM, searchShoeSize);
						}
					}
					if (null != color && color.isPresent()) {
						request.setAttribute(SearchConstants.SEARCH_COLOR, color.get());
					}

					if (null != refineSelcolor && refineSelcolor.isPresent()) {
						request.setAttribute(SearchConstants.SEARCH_COLOR, refineSelcolor.get());
					}

					if (null != searchBrand && searchBrand.isPresent()) {
						request.setAttribute(SearchConstants.SEARCH_BRAND, searchBrand.get());
					}

					if (null != searchApparelSize && searchApparelSize.isPresent()) {
						request.setAttribute(SearchConstants.APPAREL_SIZE, searchApparelSize.get());
					}

					if (null != searchGender && searchGender.isPresent()) {
						String genderName = !searchGender.get().equalsIgnoreCase(SearchConstants.KIDS)
								? catalogElementsFinder.getGenderMap().get(searchGender.get())
								: SearchConstants.KIDS;
						request.setAttribute(SearchConstants.SEARCH_GENDER, genderName);
					}
					searchHelper.populateClearRefUrl(request, query, color, searchBrand, searchGender, searchShoeSize,
							searchApparelSize);
					return;
				}
			}
			requestURI = SearchConstants.BASE_CONTEXT_PATH + query;
			log.debug("BloomreachSearchUtil:: constructQueryParams() requestURI{} from qUri", requestURI);
			String brRequestParams = BloomreachConstants.EMPTY_STRING;
			if (!query.contains(SearchConstants.BRAND_TYPE_AHEAD_URL) && !excludeDynamicurlList.contains(query)
					&& !query.contains(SearchConstants.GIFT_GUIDE_SEARCH_CONTEXT_PATH)) {
				if (query != null) {
					if (catalogElementsFinder.getBloomreachUrlQueryMap().containsKey(query)) {
						StringBuffer brqueryParam = new StringBuffer(BloomreachConstants.EMPTY_STRING);
						brqueryParam.append(SearchConstants.QUERY_);
						brqueryParam.append(catalogElementsFinder.getBloomreachUrlQueryMap().get(query));
						brRequestParams = brqueryParam.toString();
						if (null != queryParams.getProperty(BloomreachConstants.SEARCH_REDIRECT_URL)) {
							request.setAttribute(SearchConstants.URL_QUERY, query);
						}
					} else {
						StringBuffer brQueryParam = new StringBuffer(BloomreachConstants.EMPTY_STRING);
						if (null != query && query.endsWith(BloomreachConstants.OUTLET_BASEMENT_URL)) {
							query = query.replaceAll(BloomreachConstants.OUTLET_BASEMENT_URL,
									BloomreachConstants.OUTLET_BASEMENT_ORDER_URL);
						}
						String[] queryStringurls = query.split(SearchConstants.SLASH);
						String customQuery = null;
						String seourl = query;
						if (null != queryStringurls && queryStringurls.length > 1) {
							customQuery = queryStringurls[queryStringurls.length - 1];
						}
						for (int i = 0; i < queryStringurls.length; i++) {
							if (query != null && query.contains(SearchConstants.APOSTROPHE)) {
								queryStringurls[i] = queryStringurls[i].replace(SearchConstants.APOSTROPHE,
										SearchConstants.EMPTY_STRING);
							}
						}
						HashMap<String, Integer> countQuery = new HashMap<String, Integer>();
						boolean customUrl = false;
						List<Integer> list = new ArrayList<Integer>();
						AtomicInteger refinmentActiveCount = new AtomicInteger(0);
						String gender = SearchConstants.EMPTY_STRING;
						String categoryName = SearchConstants.EMPTY_STRING;
						String subCategoryName = SearchConstants.EMPTY_STRING;
						String brandName = SearchConstants.EMPTY_STRING;
						String outletName = SearchConstants.EMPTY_STRING;
						boolean isTopNavSocks = (query != null)
								&& query.contains(SearchConstants.CATEGORY_SOCKS_TOPNAV_URL);
						request.setAttribute(SearchConstants.PAGE_TITLE_SOCKS, isTopNavSocks ? isTopNavSocks : null);
						for (String queryStringurl : queryStringurls) {
							if (queryStringurl != null && !queryStringurl.isEmpty()) {
								if (catalogElementsFinder.getGender().contains(queryStringurl)) {
									if (query.contains(SearchConstants.SITE_KIDS)) {
										formBrQuery(queryStringurl, SearchConstants.KIDSGENDER, brQueryParam);
										getUrlIndex(urlOrderMap.get(SearchConstants.KIDSGENDER), list);
										request.setAttribute(BloomreachConstants.IS_GENDER, SearchConstants.SITE_KIDS);
										gender = SearchConstants.SITE_KIDS.concat(SearchConstants.SPACE)
												.concat(queryStringurl);
									} else {
										if (rrConfiguration.isEnableRankingInGenderUrl()
												&& genderUrlList.contains(query)
												&& null != catalogElementsFinder.getGenderMap().get(queryStringurl)) {
											request.setAttribute(BloomreachConstants.IS_GENDER,
													catalogElementsFinder.getGenderMap().get(queryStringurl));
											gender = queryStringurl;
										} else {
											formBrQuery(queryStringurl, SearchConstants.GENDER_PARAM, brQueryParam);
											formBrQuery(SearchConstants.GENDER_UNISEX, SearchConstants.GENDER_PARAM,
													brQueryParam);
											getUrlIndex(urlOrderMap.get(SearchConstants.GENDER_PARAM), list);
											gender = queryStringurl;
										}
									}
									formBloomrechDashBoardCategory(gender, categoryName, subCategoryName, brandName,
											request, outletName, query);
									getQueryCount(countQuery, SearchConstants.GENDER_PARAM);
									continue;
								}

								if (catalogElementsFinder.getBloomreachExcludeSearchMap() != null && query != null
										&& catalogElementsFinder.getBloomreachExcludeSearchMap().containsKey(query)
										&& rrConfiguration.isEnableBloomreachExcluded()) {
									String queryUrl = catalogElementsFinder.getBloomreachExcludeSearchMap().get(query);
									if (queryUrl != null) {
										request.setAttribute(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR, queryUrl);
									}
									request.setAttribute(SearchConstants.TYPE_KEYWORD, SearchConstants.TRUE);
									log.debug(
											"BloomreachSearchUtil :: constructQueryParams :: catagory_qr :: {0}  and search type {1}",
											queryUrl, request.getAttribute(SearchConstants.TYPE_KEYWORD));
								}
								if (catalogElementsFinder.getBrands().contains(queryStringurl)) {
									formBrQuery(queryStringurl, SearchConstants.BRAND, brQueryParam);
									getQueryCount(countQuery, SearchConstants.BRAND);
									getUrlIndex(urlOrderMap.get(SearchConstants.BRAND), list);
									refinmentActiveCount.set(refinmentActiveCount.get() + 1);
									formBloomrechDashBoardCategory(gender, categoryName, subCategoryName,
											queryStringurl, request, outletName, query);
									brandName = queryStringurl;
									continue;
								}
								if (SearchConstants.PARAM_OUTLET.equals(queryStringurl)) {
									formBrQuery(queryStringurl, SearchConstants.PARAM_OUTLET, brQueryParam);
									getQueryCount(countQuery, SearchConstants.PARAM_OUTLET);
									outletName = queryStringurl;
									if (null != catalogElementsFinder.getBloomreachDashBoardCategoryMap().get(query)) {
										request.setAttribute(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR,
												catalogElementsFinder.getBloomreachDashBoardCategoryMap().get(query));
									} else {
										request.setAttribute(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR, outletName);
									}
									getUrlIndex(urlOrderMap.get(SearchConstants.PARAM_OUTLET), list);
									refinmentActiveCount.set(refinmentActiveCount.get() + 1);
									continue;
								}

								if (SearchConstants.BASEMENT.equals(queryStringurl)) {
									formBrQuery(queryStringurl, SearchConstants.BASEMENT, brQueryParam);
									getQueryCount(countQuery, SearchConstants.BASEMENT);
									getUrlIndex(urlOrderMap.get(SearchConstants.BASEMENT), list);
									refinmentActiveCount.set(refinmentActiveCount.get() + 1);
									continue;
								}

								if (SearchConstants.NEW.equals(queryStringurl)) {
									formBrQuery(queryStringurl, SearchConstants.NEWPRODUCT, brQueryParam);
									getQueryCount(countQuery, SearchConstants.NEWPRODUCT);
									continue;
								}

								if (SearchConstants.SALE.equals(queryStringurl)) {
									formBrQuery(queryStringurl, SearchConstants.SALE, brQueryParam);
									getQueryCount(countQuery, SearchConstants.SALE);
									refinmentActiveCount.set(refinmentActiveCount.get() + 1);
									continue;
								}

								if (SearchConstants.PERSONALCARE.equals(queryStringurl)) {
									categoryName = queryStringurl;
								}

								if (catalogElementsFinder.getCategory().contains(queryStringurl)) {
									formBrQuery(queryStringurl, SearchConstants.CATAGORY, brQueryParam);
									getQueryCount(countQuery, SearchConstants.CATAGORY);
									getUrlIndex(urlOrderMap.get(SearchConstants.CATAGORY), list);
									formBloomrechDashBoardCategory(gender, queryStringurl, subCategoryName, brandName,
											request, outletName, query);
									categoryName = queryStringurl;
									continue;
								}

								if (catalogElementsFinder.getSubCategory().contains(queryStringurl)) {
									formBrQuery(queryStringurl, SearchConstants.SUBCATAGORY, brQueryParam);
									getQueryCount(countQuery, SearchConstants.SUBCATAGORY);
									getUrlIndex(urlOrderMap.get(SearchConstants.SUBCATAGORY), list);
									if (!excludedSubCategoryOrderList.contains(queryStringurl) && catalogElementsFinder
											.getBloomreachCategoryMap().containsKey(queryStringurl)
											&& !excludedCategoryList.contains(categoryName)) {
										String categoryQuery = gender.concat(SearchConstants.SPACE)
												.concat(catalogElementsFinder.getBloomreachCategoryMap()
														.get(queryStringurl))
												.concat(SearchConstants.SPACE).concat(categoryName);
										String resultUrl = rrConfiguration.isEnableGenderWithFeedChange()
												&& !StringUtils.isEmpty(gender) ? categoryQuery.trim()
														: catalogElementsFinder.getBloomreachCategoryMap()
																.get(queryStringurl);
										request.setAttribute(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR, resultUrl);
										subCategoryName = catalogElementsFinder.getBloomreachCategoryMap()
												.get(queryStringurl);
									} else if (!excludedSubCategoryOrderList.contains(queryStringurl)
											&& !excludedCategoryList.contains(categoryName)) {
										String categoryQuery = gender.concat(SearchConstants.SPACE)
												.concat(queryStringurl).concat(SearchConstants.SPACE)
												.concat(categoryName);
										String resultUrl = rrConfiguration.isEnableGenderWithFeedChange()
												&& !StringUtils.isEmpty(gender) ? categoryQuery.trim() : queryStringurl;
										request.setAttribute(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR, resultUrl);
										subCategoryName = queryStringurl;
									}
									refinmentActiveCount.set(isTopNavSocks ? refinmentActiveCount.get()
											: refinmentActiveCount.get() + 1);
									formBloomrechDashBoardCategory(gender, categoryName, subCategoryName, brandName,
											request, outletName, query);
									continue;
								}

								if (SearchConstants.LAST_CHANCE.equalsIgnoreCase(queryStringurl)) {
									formBrQuery(SearchConstants.ONE_STRING, SearchConstants.ENDANGERED.toLowerCase(),
											brQueryParam);
									getQueryCount(countQuery, SearchConstants.LAST_CHANCE);
									continue;
								}

								if (catalogElementsFinder.getColors().contains(queryStringurl)) {
									formBrQuery(queryStringurl, SearchConstants.COLOR, brQueryParam);
									getQueryCount(countQuery, SearchConstants.COLOR);
									request.setAttribute(SearchConstants.SELECTED_COLOR, queryStringurl);
									getUrlIndex(urlOrderMap.get(SearchConstants.COLOR), list);
									refinmentActiveCount.set(refinmentActiveCount.get() + 1);
									continue;
								}

								if (catalogElementsFinder.getSize().contains(queryStringurl)) {
									formBrQuery(queryStringurl, SearchConstants.KIDS_TYPE, brQueryParam);
									getQueryCount(countQuery, SearchConstants.KIDS_TYPE);
									request.setAttribute(BloomreachConstants.URL_KIDS_TYPE, queryStringurl);
									getUrlIndex(urlOrderMap.get(SearchConstants.KIDS_TYPE), list);
									refinmentActiveCount.set(refinmentActiveCount.get() + 1);
									continue;
								}

								if (catalogElementsFinder.getSportsMap().entrySet().stream()
										.anyMatch(sports -> sports.getValue().equalsIgnoreCase(queryStringurl))) {
									formBrQuery(sportsMap.get(queryStringurl), SearchConstants.SPORTS_TYPE,
											brQueryParam);
									refinmentActiveCount.set(refinmentActiveCount.get() + 1);
									continue;
								}

								if (catalogElementsFinder.getShoeType().contains(queryStringurl)) {
									formBrQuery(queryStringurl, SearchConstants.SHOE_TYPE, brQueryParam);
									getQueryCount(countQuery, SearchConstants.SHOE_TYPE);
									getUrlIndex(urlOrderMap.get(SearchConstants.SHOE_TYPE), list);
									refinmentActiveCount.set(refinmentActiveCount.get() + 1);
									continue;
								}

								if (null != queryStringurl
										&& queryStringurl.equalsIgnoreCase(SearchConstants.PERSONAL_CARE)) {
									formBrQuery(SearchConstants.PGC_CODE_VALUE, SearchConstants.CATAGORY, brQueryParam);
									getQueryCount(countQuery, SearchConstants.CATAGORY);
									request.setAttribute(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR,
											SearchConstants.PGC_CODE_VALUE);
									continue;
								}

								if (null != queryStringurl
										&& queryStringurl.equalsIgnoreCase(SearchConstants.EQUIPMENT)) {
									formBrQuery(SearchConstants.PGC_CODE_VALUE, SearchConstants.CATAGORY, brQueryParam);
									getQueryCount(countQuery, SearchConstants.CATAGORY);
									categoryName = queryStringurl;
									request.setAttribute(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR,
											SearchConstants.PGC_CODE_VALUE);
									continue;
								}

								if (null != customQuery && customQuery.equals(queryStringurl)
										&& !catalogElementsFinder.getBrands().contains(customQuery)
										&& !catalogElementsFinder.getColors().contains(customQuery)
										&& !catalogElementsFinder.getCategory().contains(customQuery)
										&& !catalogElementsFinder.getSubCategory().contains(customQuery)) {
									String custom = customQuery.substring(0, 1).toUpperCase();
									String remainStr = customQuery.substring(1, customQuery.length());
									customQuery = custom + remainStr;
									String newQuery = customQuery.replace(SearchConstants.HYPHEN_STRING,
											SearchConstants.PLUS);
									request.setAttribute(BloomreachConstants.QPARAMS.QUERY, newQuery);
									customUrl = true;
									continue;
								}
							}
						}
						request.setAttribute(SearchConstants.REFINMENT_ACTIVE_COUNT,
								Integer.toString(refinmentActiveCount.get()));
						boolean isCustomQuery = true;
						if (customUrl) {
							SeoContent seoRepo = getSeoContent(seourl);
							if (seoRepo == null && query.equalsIgnoreCase(SearchConstants.CATEGORY_URL)) {
								isCustomQuery = false;
								request.setAttribute(SearchConstants.IS_CUSTOMQUERY, SearchConstants.FALSE);

							}
						}
						boolean isOrderUrl = true;
						if (list.size() > 1) {
							for (int i = 1; i < list.size(); i++) {
								if (list.get(i) > (list.get(i - 1))) {
									continue;
								} else {
									isOrderUrl = false;
									request.setAttribute(SearchConstants.IS_CUSTOMQUERY, SearchConstants.FALSE);
									break;
								}
							}
						}
						boolean multipletCount = false;
						if (countQuery != null && !countQuery.isEmpty()) {
							multipletCount = countQuery.entrySet().stream().anyMatch(map -> map.getValue() > 1);
							if (multipletCount) {
								request.setAttribute(SearchConstants.QUERY_COUNT, SearchConstants.TRUE);
							}
						}

						if (bQuery != null && !bQuery.isEmpty()) {
							String[] brands = bQuery.split(SearchConstants.COMMA);
							for (String brand : brands) {
								formBrQuery(brand, SearchConstants.BRAND, brQueryParam);
							}
						}

						if (cQuery != null && !cQuery.isEmpty()) {
							String[] colors = cQuery.split(SearchConstants.COMMA);
							request.setAttribute(SearchConstants.SELECTED_COLOR, cQuery);
							for (String color : colors) {
								formBrQuery(color, SearchConstants.COLOR, brQueryParam);
							}
						}

						if (catQuery != null && !catQuery.isEmpty()) {
							String[] categorys = catQuery.split(SearchConstants.COMMA);
							for (String category : categorys) {
								formBrQuery(category, SearchConstants.SUBCATAGORY, brQueryParam);
							}
						}

						if (shoeQuery != null && !shoeQuery.isEmpty()) {
							String[] shoetypes = shoeQuery.split(SearchConstants.COMMA);
							for (String shoeType : shoetypes) {
								formBrQuery(shoeType, SearchConstants.SHOE_TYPE, brQueryParam);
							}
						}

						if (sportsQuery != null && !sportsQuery.isEmpty()) {
							String[] sportsTypes = sportsQuery.split(SearchConstants.COMMA);
							for (String element : sportsTypes) {
								if (catalogElementsFinder.getSportsMap().entrySet().stream()
										.anyMatch(sports -> sports.getValue().equalsIgnoreCase(element))) {
									formBrQuery(sportsMap.get(element), SearchConstants.SPORTS_TYPE, brQueryParam);
								}
							}
						}

						String selectedRef = BloomreachConstants.EMPTY_STRING;
						try {
							if (brQueryParam.length() > 0) {
								selectedRef = URLEncoder.encode(
										brQueryParam.toString().substring(0, brQueryParam.length() - 1)
												.replace(BloomreachConstants.R_EQUAL, BloomreachConstants.EMPTY_STRING),
										StandardCharsets.UTF_8.toString());
							}
						} catch (UnsupportedEncodingException pUnsupportedEncodingException) {
							log.error("BloomreachSearchUtil::constructQueryParams brQueryParam {} exception",
									brQueryParam, pUnsupportedEncodingException);
						}
						request.setAttribute(BloomreachConstants.BR_QUERY_PARAM_DYNAMIC_URL_REFINMENT, selectedRef);
						if (rQuery != null && !rQuery.isEmpty() && !brQueryParam.toString().isEmpty()) {
							rQuery = rQuery.replace(SearchConstants.URL_DELIMETER2, SearchConstants.STRING_26);
							brQueryParam.append(rQuery);
						}

						if (brQueryParam.toString().endsWith(SearchConstants.COMMA)) {
							brRequestParams = brQueryParam.substring(0, brQueryParam.length() - 1);
						} else {
							brRequestParams = brQueryParam.toString();
						}
						log.debug("BloomreachSearchUtil constructQueryParams() dynamicUrl:: brRequestParams {}",
								brRequestParams);
						request.setAttribute(SearchConstants.REMOVE_CATAGORY, SearchConstants.TRUE);
						request.setAttribute(SearchConstants.URL_QUERY, query);
						request.setAttribute(SearchConstants.DYNAMIC_URL_REFINMENT, SearchConstants.TRUE);
						request.setAttribute(BloomreachConstants.R_QUERY, rQuery);
					}
				}
			}
			log.debug("BloomreachSearchUtil constructQueryParams()  brRequestParams {} final ", brRequestParams);
			Map<String, String> queryParmasMap = Arrays.stream(brRequestParams.split(BloomreachConstants.URL_DELIMETER))
					.map(s -> s.split(BloomreachConstants.EQUAL))
					.collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : BloomreachConstants.EMPTY_STRING));
			queryParmasMap.forEach((key, value) -> {
				value = value.replace(SearchConstants.PLUS, SearchConstants.SPACE);
				value = value.replace(SearchConstants.OST_LOWER, SearchConstants.OST_UPPER);
				request.setAttribute(key, value.replace(SearchConstants.STRING_26, SearchConstants.URL_DELIMETER2));
			});
			log.debug("BloomreachSearchUtil " + "constructQueryParams() END ::", brRequestParams);
		}
	}

	/**
	 * This method is used to form query param formBrQuery
	 * 
	 * @param queryStringurl
	 * @param queryType
	 * @param brQueryParam
	 */
	private void formBrQuery(String queryStringurl, String queryType, StringBuffer brQueryParam) {
		log.debug("BloomreachSearchUtil::formBrQuery() START ::queryType::{}::brQueryParam::{}", queryType,
				brQueryParam);
		if (brQueryParam.toString().isEmpty()) {
			brQueryParam.append(BloomreachConstants.R_EQUAL);
		}
		brQueryParam.append((urlMap.get(queryType)));
		brQueryParam.append(SearchConstants.COLON);
		if (BloomreachConstants.GENDER.equals(queryType)) {
			brQueryParam.append(catalogElementsFinder.getGenderMap().get(queryStringurl));
		} else {
			if (catalogElementsFinder.getQueryMap().containsKey(queryStringurl)
					&& !queryType.equalsIgnoreCase(SearchConstants.SPORTS_TYPE)) {
				brQueryParam.append(catalogElementsFinder.getQueryMap().get(queryStringurl));
			} else if (catalogElementsFinder.getBloomreachBrandMap().containsKey(queryStringurl)) {
				brQueryParam.append(catalogElementsFinder.getBloomreachBrandMap().get(queryStringurl));
			} else {
				brQueryParam.append(WordUtils
						.capitalizeFully(queryStringurl, BloomreachConstants.SINGLE_QUOTES_SPACE,
								BloomreachConstants.SINGLE_QUOTES_HYPHEN)
						.replace(BloomreachConstants.HYPHEN, BloomreachConstants.PLUS));
			}
		}
		brQueryParam.append(SearchConstants.COMMA);
		log.debug("BloomreachSearchUtil::formBrQuery() END ::brQueryParam::{}", brQueryParam);
	}

	/**
	 * @param countQuery
	 * @param param
	 */
	private void getQueryCount(HashMap<String, Integer> countQuery, String param) {
		if (countQuery.containsKey(param)) {
			int count = countQuery.get(param);
			countQuery.put(param, count + 1);
		} else {
			countQuery.put(param, 1);
		}
	}

	/**
	 * @param index
	 * @param list
	 */
	private void getUrlIndex(String index, List<Integer> list) {
		int indexPosition = Integer.parseInt(index);
		list.add(indexPosition);
	}

	/**
	 * This method will form q param url same as bloomreach dashboard
	 * formBloomrechDashBoardCategory
	 * 
	 * @param gender
	 * @param categoryName
	 * @param subCategoryName
	 * @param brandName
	 * @param request
	 * @param outletName
	 * @param query
	 */
	private void formBloomrechDashBoardCategory(String gender, String categoryName, String subCategoryName,
			String brandName, HttpServletRequest request, String outletName, String query) {
		if (null != request.getAttribute(SearchConstants.TYPE_KEYWORD) || excludedCategoryList.contains(categoryName)) {
			return;
		}
		if (null != catalogElementsFinder.getBloomreachDashBoardCategoryMap().get(query)) {
			request.setAttribute(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR,
					catalogElementsFinder.getBloomreachDashBoardCategoryMap().get(query));
			return;
		} else if (catalogElementsFinder.getBloomreachDashBoardCategoryMap().entrySet().parallelStream()
				.anyMatch(url -> query.contains(url.getKey()))) {
			String resultCategory = (gender.concat(SearchConstants.SPACE).concat(categoryName)
					.concat(SearchConstants.SPACE).concat(subCategoryName).concat(SearchConstants.SPACE))
					.replaceAll(SearchConstants.SPACE_REGEX, SearchConstants.SPACE).trim();
			request.setAttribute(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR, resultCategory);
			return;
		}
		if (!StringUtils.isEmpty(gender) && gender.equalsIgnoreCase(SearchConstants.UNISEX)) {
			gender = SearchConstants.EMPTY_STRING;
		}
		String resultCategory = (gender.concat(SearchConstants.SPACE).concat(categoryName))
				.replaceAll(SearchConstants.SPACE_REGEX, SearchConstants.SPACE).trim();
		request.setAttribute(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR, resultCategory);
		if (request.getAttribute(SearchConstants.PAGE_TITLE_SOCKS) != null && StringUtils.isNotEmpty(subCategoryName)) {
			request.setAttribute(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR, subCategoryName);
		}
	}

	/**
	 * This method is used to get seo data form seoContentRepository
	 * 
	 * @param url
	 * @return The SEO content corresponding to the provided URL, or null if not
	 *         found
	 */
	public SeoContent getSeoContent(String url) {
		log.debug("BloomreachSearchUtil::getSeoContent() url:: {}", url);
		SeoContent seoContent = seoContentRepository.findBySeoUrl(url);
		return seoContent;
	}

	/**
	 * This method is used to populate the request parameter populateRequestParam
	 * 
	 * @param params
	 * @return
	 */
	public Map<String, String> populateRequestParam(Map<String, String> params) {
		log.debug("BloomreachSearchUtil::populateRequestParam: START :: params={}", params);
		StringBuffer host = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
				.getRequestURL();
		String domain = null;
		boolean isSort = false;
		String smartZone = (String) params.get(BloomreachConstants.QPARAMS.SZ);
		try {
			URL url = new URL(host.toString());
			domain = url.getProtocol().concat(BloomreachConstants.URL_SLASH).concat(url.getHost());
		} catch (MalformedURLException exception) {
			log.error("BloomreachSearchUtil::populateRequestParam::exception={}", exception);
		}
		String skip = (String) params.get(BloomreachConstants.QPARAMS.SKIP);
		String uid = (String) params.get(BloomreachConstants.UID);
		String searchQ = (String) params.get(BloomreachConstants.Q);
		String sort = null;
		if (!StringUtils.isEmpty((String) params.get(BloomreachConstants.QPARAMS.S))) {
			sort = (String) params.get(BloomreachConstants.QPARAMS.S);
		}
		if (null != smartZone && SearchConstants.TRUE.equalsIgnoreCase(smartZone)) {
			isSort = true;
		}
		Map<String, String> constructParam = new HashMap<>();
		constructParam.put(BloomreachConstants.ACCOUNT_ID, bloomreachConfiguration.getAccountId());
		constructParam.put(BloomreachConstants.AUTH_KEY, bloomreachConfiguration.getAuthKey());
		constructParam.put(BloomreachConstants.DOMAIN_KEY, bloomreachConfiguration.getDomainKey());
		constructParam.put(BloomreachConstants.FL,
				String.join(BloomreachConstants.COMMA, bloomreachConfiguration.getFl()));
		constructParam.put(BloomreachConstants.URL, domain);
		constructParam.put(BloomreachConstants.REF_URL, domain);
		constructParam.put(BloomreachConstants.REQUEST_TYPE, bloomreachConfiguration.getRequestType());
		constructParam.put(BloomreachConstants.ROWS, BloomreachConstants.ROWS_COUNT);
		if (params.get(BloomreachConstants.PRODUCT_FIELD.CATAGORY_QR) != null) {
			constructParam.put(BloomreachConstants.SEARCH_TYPE, bloomreachConfiguration.getCatagorytype());
		} else {
			constructParam.put(BloomreachConstants.SEARCH_TYPE, bloomreachConfiguration.getSearchType());
		}
		if (!StringUtils.isEmpty(searchQ)) {
			constructParam.put(BloomreachConstants.Q, searchQ);
		}
		if (!StringUtils.isEmpty(uid)) {
			constructParam.put(BloomreachConstants.UID_PARAM, uid);
		}
		if (!StringUtils.isEmpty(sort) && !isSort) {
			constructParam.put(BloomreachConstants.SORT, sortOptionsMap.get(sort));
		}
		if (!StringUtils.isEmpty(skip)) {
			constructParam.put(BloomreachConstants.START, skip);
		} else {
			constructParam.put(BloomreachConstants.START, BloomreachConstants.START_COUNT);
		}
		log.debug("BloomreachSearchUtil::populateRequestParam:: END...");
		return constructParam;
	}

	/**
	 * This method is used to form the bloomreach url
	 * 
	 * @param params
	 * @return
	 */
	public String formBloomreachParamUrl(Map<String, String> params) {
		log.debug("BloomreachSearchUtil::formBloomreachParamUrl:: START...");
		StringBuilder result = new StringBuilder();
		if (null != params) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String value = entry.getValue();
				try {
					result.append(URLEncoder.encode(entry.getKey(), BloomreachConstants.UTF_8));
					result.append(BloomreachConstants.EQUAL);
					if (!entry.getKey().equals(BloomreachConstants.SORT)) {
						value = URLEncoder.encode(entry.getValue(), BloomreachConstants.UTF_8);
					}
					result.append(value);
					result.append(BloomreachConstants.URL_DELIMETER);
				} catch (UnsupportedEncodingException exception) {
					log.error("BloomreachSearchUtil::formBloomreachParamUrl params {} exception={}", params, exception);
				}
			}
		}
		log.debug("BloomreachSearchUtil::formBloomreachParamUrl:: END...");
		return result.toString();
	}

	/**
	 * @param url
	 * @return
	 */
	public String getPageURL(String url) {
		log.debug("BloomreachSearchUtil::getPageURL() START ::url::{}", url);
		String finalUrl = "";
		String pageUrl = null;
		String[] queryStringurls = url.split(SearchConstants.SLASH);
		int mapLength = 0;
		int maxLength = url.contains(SearchConstants.SITE_KIDS) ? 6 : 5;
		for (String element : queryStringurls) {
			if ((mapLength > 1 && mapLength < maxLength) && !catalogElementsFinder.getColors().contains(element)
					&& !catalogElementsFinder.getBrands().contains(element)) {
				finalUrl += element.concat(SearchConstants.HYPHEN_STRING);
			}
			mapLength++;
		}
		if (null != finalUrl && finalUrl.length() > 0) {
			StringBuffer removeIndex = new StringBuffer(finalUrl);
			pageUrl = removeIndex.deleteCharAt(finalUrl.length() - 1).toString();
		}
		log.debug("BloomreachSearchUtil::getPageURL() END ::pageUrl::{}", pageUrl);
		return pageUrl;
	}

	/**
	 * This method is used to populate selected navigations selectedNavigation
	 * 
	 * @param param
	 * @param queryParams
	 * @return
	 */
	public Map<String, String> selectedNavigation(String param, Properties queryParams) {
		log.debug("BloomreachSearchUtil :: selectedNavigation :: param: {}", param);
		String urlQuery = queryParams.getProperty(SearchConstants.URL_QUERY);
		Map<String, String> selectedNavigationMap = new LinkedHashMap<>();
		if (!StringUtils.isEmpty(param)) {
			param = param.replaceAll(BloomreachConstants.PERCENTAGE_3A, SearchConstants.COLON)
					.replaceAll(BloomreachConstants.PERCENTAGE_20, SearchConstants.SPACE)
					.replaceAll(BloomreachConstants.PERCENTAGE_2C, BloomreachConstants.COMMA)
					.replaceAll(BloomreachConstants.PERCENTAGE_28, BloomreachConstants.OPEN_BRACKET)
					.replaceAll(BloomreachConstants.PERCENTAGE_29, BloomreachConstants.CLOSE_BRACKER);
			String[] navigationList = param.split(BloomreachConstants.COMMA);
			for (String navigation : navigationList) {
				String[] splitNavs = navigation.split(SearchConstants.COLON);
				if (null != splitNavs && splitNavs.length > 1) {
					String type = splitNavs[0];
					String value = splitNavs[1];
					if (null != urlQuery && type.equalsIgnoreCase(SearchConstants.SPORTS_TYPE)) {
						value = catalogElementsFinder.getSportsMap().get(value);
					}
					selectedNavigationMap.put(value, type);
				}
			}
		}
		log.debug("BloomreachSearchUtil :: selectedNavigation :: selectedNavigationMap: {}", selectedNavigationMap);
		return selectedNavigationMap;
	}

	public List<CategoryItemDTO> getCategoryItem(String query) {
		log.debug("BloomreachSearchUtil :: getCategoryItem :: query: {}", query);
		List<CategoryItemDTO> itemList = null;
		if (StringUtils.isEmpty(query)) {

		}
		itemList = rrsCategoryMapRepository.getCategoryItem(siteId, query);
		log.debug("BloomreachSearchUtil :: getCategoryItem :: itemList: {}", itemList);
		return itemList;
	}

}
