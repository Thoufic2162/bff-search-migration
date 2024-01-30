package com.roadrunner.search.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.gson.Gson;
import com.roadrunner.search.config.BloomreachConfiguration;
import com.roadrunner.search.config.RRConfiguration;
import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.domain.DCSPrice;
import com.roadrunner.search.domain.DCSProductChildSkus;
import com.roadrunner.search.domain.RRSProductRating;
import com.roadrunner.search.domain.RRSProductWeb;
import com.roadrunner.search.domain.RRSSku;
import com.roadrunner.search.dto.BloomreachSearchResponseDTO;
import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.CrossSellProductsDTO;
import com.roadrunner.search.dto.PriceDTO;
import com.roadrunner.search.dto.ProductDTO;
import com.roadrunner.search.dto.RecommendationProductDTO;
import com.roadrunner.search.dto.UpSellProductsDTO;
import com.roadrunner.search.helper.ProductSkuHelper;
import com.roadrunner.search.repo.DCSPriceRepository;
import com.roadrunner.search.repo.DCSProductSkusRepository;
import com.roadrunner.search.repo.RRSProductRatingRepository;
import com.roadrunner.search.repo.RRSProductRepository;
import com.roadrunner.search.repo.RRSProductWebRepository;
import com.roadrunner.search.repo.RRSSkuRepository;
import com.roadrunner.search.service.BloomreachSearchRecommendationService;
import com.roadrunner.search.service.BloomreachSearchService;
import com.roadrunner.search.tools.BloomreachProductSearchResults;
import com.roadrunner.search.util.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@ConfigurationProperties(prefix = "bloomreachsearchrecommendation")
@Getter
@Setter
public class BloomreachSearchRecommendationServiceImpl implements BloomreachSearchRecommendationService {

	@Autowired
	private Gson gson;

	@Autowired
	private RRConfiguration rrConfiguration;

	@Autowired
	private BloomreachConfiguration bloomreachConfiguration;

	@Autowired
	private BloomreachSearchService bloomreachSearchService;

	@Autowired
	private BloomreachProductSearchResults bloomreachProductSearchResults;

	@Autowired
	private RRSProductRepository rrsProductRepository;

	@Autowired
	private RRSSkuRepository rrsSkuRepository;

	@Autowired
	private DCSPriceRepository dcsPriceRepository;

	@Autowired
	private DCSProductSkusRepository dcsProductSkusRepository;

	@Autowired
	private RRSProductRatingRepository rrsProductRatingRepository;

	@Autowired
	private RRSProductWebRepository productWebRepository;

	@Autowired
	private ProductSkuHelper productSkuHelper;

	private Map<String, String> bloomreachUrlMap;
	private List<String> skipList;
	private Map<String, String> nonPromotionalProducts;
	private Map<String, String> vipURLMap;
	private int upsellProductsSize;

	@Override
	public List<RecommendationProductDTO> searchRecommendations(Object profile, Map<String, String> refParams,
			boolean kidsSite, boolean isWearometer) {

		if (CollectionUtils.isEmpty(refParams)) {
			log.debug("BloomreachSearchRecommendationServiceImpl :: searchRecommendations: input params are empty");
		}
		profile = new Object();// need to set up the profile data
		BloomreachSearchResponseDTO bloomreachSearchResponse = new BloomreachSearchResponseDTO();
		BloomreachSearchResultsDTO upSellProducts = new BloomreachSearchResultsDTO();
		List<RecommendationProductDTO> upsellProductResults = new ArrayList<>();
		bloomreachSearchResponse = doSearch(refParams);
		bloomreachProductSearchResults.getProductResults(bloomreachSearchResponse, null, upSellProducts);
		upsellProductResults = upSellProducts.getResults();
		if (null != profile && !isWearometer) {
			upsellProductResults = populateVIPProducts(profile, upSellProducts.getResults());
			log.debug("BloomreachSearchRecommendationServiceImpl::searchRecommendations::  upSellProducts {}",
					upSellProducts);
		}
		return upsellProductResults;
	}

	@Override
	public List<RecommendationProductDTO> searchRecommendationsForUpSell(Object profile, Map<String, String> refParams,
			boolean isWearometer, UpSellProductsDTO pUpSellProductsDTO) {
		if (CollectionUtils.isEmpty(refParams)) {
			log.debug(
					"BloomreachSearchRecommendationServiceImplImpl:: searchRecommendationsForUpSell: input params are empty");
		}
		profile = new Object();// need to set up the profile data
		BloomreachSearchResponseDTO bloomreachSearchResponse = new BloomreachSearchResponseDTO();
		BloomreachSearchResultsDTO upSellProducts = new BloomreachSearchResultsDTO();
		List<RecommendationProductDTO> upsellProductResults = new ArrayList<>();
		String methodName = refParams.get(BloomreachConstants.RECOMMENDATION_METHOD);
		bloomreachSearchResponse = doSearch(refParams);
		bloomreachProductSearchResults.getProductResults(bloomreachSearchResponse, null, upSellProducts);
		upsellProductResults = upSellProducts.getResults();
		pUpSellProductsDTO.setMetadata(bloomreachSearchResponse.getMetadata());
		if (null != profile && !isWearometer && null != methodName
				&& !methodName.contains(BloomreachConstants.OUTFIT_YOUR_RUN)
				&& !methodName.contains(BloomreachConstants.BEST_SELLER)) {
			upsellProductResults = populateVIPProducts(profile, upSellProducts.getResults());
			log.debug("BloomreachSearchRecommendationServiceImpl::searchRecommendationsForUpSell::  upSellProducts {}",
					upSellProducts);
		}
		return upsellProductResults;
	}

	@Override
	public List<RecommendationProductDTO> searchRecommendationsForCrossSell(Object profile,
			Map<String, String> refParams, boolean isWearometer, CrossSellProductsDTO crossSellProductsDTO) {
		if (CollectionUtils.isEmpty(refParams)) {
			log.debug(
					"BloomreachSearchRecommendationServiceImpl :: searchRecommendationsForCrossSell: input params are empty");
		}
		profile = new Object();// need to set up the profile data
		BloomreachSearchResponseDTO bloomreachSearchResponse = new BloomreachSearchResponseDTO();
		BloomreachSearchResultsDTO crossSellProducts = new BloomreachSearchResultsDTO();
		List<RecommendationProductDTO> crossSellProductResults = new ArrayList<>();
		bloomreachSearchResponse = doSearch(refParams);
		bloomreachProductSearchResults.getProductResults(bloomreachSearchResponse, null, crossSellProducts);
		crossSellProductResults = crossSellProducts.getResults();
		if (null != profile && !isWearometer && null != crossSellProductResults && !crossSellProductResults.isEmpty()) {
			crossSellProductsDTO.setMetadata(bloomreachSearchResponse.getMetadata());
			log.debug(
					"BloomreachSearchRecommendationService::searchRecommendationsForCrossSell::  crossSellProducts {}",
					crossSellProducts);
		}
		return crossSellProductResults;
	}

	private BloomreachSearchResponseDTO doSearch(Map<String, String> refParams) {
		BloomreachSearchResponseDTO bloomreachSearchResponseDTO = new BloomreachSearchResponseDTO();
		String url;
		String apparel = refParams.get(SearchConstants.IS_APPAREL);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		log.debug("BloomreachSearchRecommendationServiceImpl::doSearch START...{}", stopWatch.getTime());
		boolean isApparel = false;
		if (null != apparel && apparel.equalsIgnoreCase(SearchConstants.TRUE)) {
			isApparel = true;
		}
		boolean isOutlet = false;
		if (null != refParams.get(SearchConstants.WEB_PGC_CODE_PARAM)
				&& null != refParams.get(SearchConstants.PARAM_OUTLET)) {
			if (refParams.get(SearchConstants.WEB_PGC_CODE_PARAM).equalsIgnoreCase(SearchConstants.SHOES)
					&& refParams.get(SearchConstants.PARAM_OUTLET).equalsIgnoreCase(SearchConstants.OUTLET)) {
				isOutlet = true;
			}
		}
		try {
			if (!rrConfiguration.isEnablePathwayRecommendation() || isApparel || isOutlet) {
				url = MessageFormat.format(bloomreachConfiguration.getSearchApiUrl(),
						getParamsString(populateRequestParam(), refParams));
			} else {
				url = MessageFormat.format(bloomreachConfiguration.getPathWayUrl(),
						bloomreachConfiguration.getRecommendationId()
								.get(refParams.get(BloomreachConstants.RECOMMENDATION_METHOD)),
						populatePathwaysRequestParam(refParams));
			}
			log.debug("BloomreachSearchRecommendationServiceImpl :: doSearch: bloomreachSearchUrl{}", url);
			String responseJson = bloomreachSearchService.bloomreachApiCall(url);
			if (null != responseJson) {
				bloomreachSearchResponseDTO = gson.fromJson(responseJson.toString(), BloomreachSearchResponseDTO.class);
			}
		} catch (IOException ioException) {
			log.error("BloomreachSearchRecommendationServiceImpl::doSearch::ioException={}", ioException);
		}
		stopWatch.stop();
		log.debug("BloomreachSearchRecommendationServiceImpl::doSearch END...{}", stopWatch.getTime());
		return bloomreachSearchResponseDTO;
	}

	public Map<String, String> populateRequestParam() {
		String domain = null;
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		if (null != request) {
			StringBuffer host = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getRequestURL();
			try {
				URL url = new URL(host.toString());
				domain = url.getProtocol() + BloomreachConstants.URL_SLASH + url.getHost();
			} catch (MalformedURLException malformedURLException) {
				log.error("BloomreachSearchRecommendationServiceImpl::populateRequestParam::malformedURLException={}",
						malformedURLException);
			}
		} else {
			domain = bloomreachConfiguration.getRefUrl();
		}

		Map<String, String> constructParam = new HashMap<>();
		constructParam.put(BloomreachConstants.ACCOUNT_ID, bloomreachConfiguration.getAccountId());
		constructParam.put(BloomreachConstants.AUTH_KEY, bloomreachConfiguration.getAuthKey());
		constructParam.put(BloomreachConstants.DOMAIN_KEY, bloomreachConfiguration.getDomainKey());
		constructParam.put(BloomreachConstants.FL,
				String.join(BloomreachConstants.COMMA, bloomreachConfiguration.getFl()));
		constructParam.put(BloomreachConstants.URL, domain);
		constructParam.put(BloomreachConstants.REF_URL, domain);
		constructParam.put(BloomreachConstants.SEARCH_TYPE, bloomreachConfiguration.getSearchType());
		constructParam.put(BloomreachConstants.REQUEST_TYPE, bloomreachConfiguration.getRequestType());
		constructParam.put(BloomreachConstants.ROWS, BloomreachConstants.ROWS_COUNT);
		constructParam.put(BloomreachConstants.START, BloomreachConstants.START_COUNT);
		constructParam.put(BloomreachConstants.SORT, BloomreachConstants.BEST_SELLER_SORT);
		return constructParam;
	}

	public String getParamsString(Map<String, String> pRefParams, Map<String, String> refParams) throws IOException {
		String resultString = null;
		if (null != pRefParams) {
			StringBuilder result = new StringBuilder();
			for (Map.Entry<String, String> entry : pRefParams.entrySet()) {
				String value = entry.getValue();
				result.append(URLEncoder.encode(entry.getKey(), BloomreachConstants.UTF_8));
				result.append(BloomreachConstants.EQUAL);
				if (!entry.getKey().equals(BloomreachConstants.SORT)) {
					value = URLEncoder.encode(entry.getValue(), BloomreachConstants.UTF_8);
				}
				result.append(value);
				result.append(BloomreachConstants.URL_DELIMETER);
			}
			for (Map.Entry<String, String> entry : refParams.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (skipList.contains(key)) {
					continue;
				}
				for (Map.Entry<String, String> urlMap : bloomreachUrlMap.entrySet()) {
					if (key.equalsIgnoreCase(urlMap.getKey())) {
						key = urlMap.getValue();
					}
				}
				value = value.replaceAll(BloomreachConstants.URL_DELIMETER, BloomreachConstants.PERCENTAGE_26)
						.replaceAll(SearchConstants.SPACE, BloomreachConstants.PERCENTAGE_20)
						.replaceAll(BloomreachConstants.PERCENTAGE_3A, SearchConstants.COLON)
						.replaceAll(BloomreachConstants.PERCENTAGE_2C, BloomreachConstants.COMMA);
				result.append(BloomreachConstants.PARAMETER_FQ + key + SearchConstants.COLON
						+ BloomreachConstants.QUOTES + value + BloomreachConstants.QUOTES);
				result.append(BloomreachConstants.URL_DELIMETER);
			}
			result.append(BloomreachConstants.PARAMETER_Q + SearchConstants.STAR);
			resultString = result.toString();

		}
		return resultString;
	}

	public String populatePathwaysRequestParam(Map<String, String> pRefParams) {
		log.debug(
				"BloomreachSearchRecommendationServiceImpl :: populatePathwaysRequestParam :: START :: pRefParams: {0}",
				pRefParams);
		String recommendationMethod = pRefParams.get(BloomreachConstants.RECOMMENDATION_METHOD);
		StringBuffer host = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
				.getRequestURL();
		String uid = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
				.getParameter(BloomreachConstants.UID_PARAM);
		String domain = null;
		String productId = pRefParams.get(BloomreachConstants.PRODUCT_FIELD.PRODUCT_ID);
		try {
			URL url = new URL(host.toString());
			domain = url.getProtocol() + BloomreachConstants.URL_SLASH + url.getHost();
		} catch (MalformedURLException malformedURLException) {
			log.error(
					"BloomreachSearchRecommendationServiceImpl::populatePathwaysRequestParam malformedURLException={}",
					malformedURLException);
		}
		Map<String, String> constructParam = new HashMap<>();
		constructParam.put(BloomreachConstants.ACCOUNT_ID, bloomreachConfiguration.getAccountId());
		constructParam.put(BloomreachConstants.DOMAIN_KEY, bloomreachConfiguration.getDomainKey());
		if (null != recommendationMethod && recommendationMethod.equalsIgnoreCase(BloomreachConstants.BEST_SELLER)) {
			constructParam.put(BloomreachConstants.FIELDS,
					String.join(BloomreachConstants.COMMA, bloomreachConfiguration.getBestSellerField()));
		} else {
			constructParam.put(BloomreachConstants.FIELDS,
					String.join(BloomreachConstants.COMMA, bloomreachConfiguration.getFields()));
		}

		constructParam.put(BloomreachConstants.URL, domain);
		constructParam.put(BloomreachConstants.REF_URL, domain);
		if (null != recommendationMethod && !(recommendationMethod.equalsIgnoreCase(BloomreachConstants.BEST_SELLER)
				|| recommendationMethod.equalsIgnoreCase(BloomreachConstants.RECENTLY_VIEWED_PRODUCTS))) {
			constructParam.put(BloomreachConstants.SORT, BloomreachConstants.BEST_SELLER_SORT);
		}
		if (null != uid) {
			constructParam.put(BloomreachConstants.UID_PARAM, bloomreachConfiguration.getBr_uid());
		} else {
			constructParam.put(BloomreachConstants.UID_PARAM, bloomreachConfiguration.getBr_uid());
		}

		if (null != productId) {
			constructParam.put(BloomreachConstants.ITEM_IDS, productId);
		}
		if (null != recommendationMethod
				&& (recommendationMethod.contains(BloomreachConstants.RECENTLY_VIEWED_PRODUCTS))
				&& pRefParams.containsKey(BloomreachConstants.USER_ID)) {
			constructParam.put(BloomreachConstants.USER_ID, pRefParams.get(BloomreachConstants.USER_ID));
		}
		if (null != recommendationMethod && (recommendationMethod.contains(BloomreachConstants.OUTFIT_YOUR_RUN)
				|| recommendationMethod.contains(BloomreachConstants.TOP_PICKS_FOR_YOU))) {
			String gender = BloomreachConstants.QUOTES + pRefParams.get(BloomreachConstants.PRODUCT_FIELD.GENDER_TEXT)
					+ BloomreachConstants.QUOTES;
			String cat_id = pRefParams.get(BloomreachConstants.PRODUCT_FIELD.GENDER_TEXT)
					.replaceAll(BloomreachConstants.SINGLE_QUOTES, BloomreachConstants.EMPTY_STRING)
					.concat(SearchConstants.SPACE).concat(BloomreachConstants.APPAREL);
			constructParam.put(BloomreachConstants.CAT_ID, cat_id);
			if (null != pRefParams.get(BloomreachConstants.IS_GIFTCARD)
					&& pRefParams.get(BloomreachConstants.IS_GIFTCARD).equalsIgnoreCase(SearchConstants.TRUE)) {
				constructParam.put(
						BloomreachConstants.BRAND_PARAMETER.concat(BloomreachConstants.BRAND_KORSA)
								.concat(BloomreachConstants.AND).concat(BloomreachConstants.RRS),
						BloomreachConstants.FILTER_FACET);
				constructParam.put(BloomreachConstants.GENDER_TEXT.concat(gender).concat(BloomreachConstants.AND)
						.concat(BloomreachConstants.UNISEX_PARAM), BloomreachConstants.FILTER_FACET);
				constructParam.put(BloomreachConstants.SORT, BloomreachConstants.BEST_SELLER_SORT);
			} else {
				constructParam.put(BloomreachConstants.BRAND_PARAMETER.concat(BloomreachConstants.BRAND_KORSA),
						BloomreachConstants.FILTER_FACET);
				constructParam.put(BloomreachConstants.GENDER_TEXT.concat(gender), BloomreachConstants.FILTER_FACET);
			}

		} else if (null != recommendationMethod
				&& (recommendationMethod.contains(BloomreachConstants.YOU_MAY_ALSO_LIKE))) {
			String gender = BloomreachConstants.QUOTES + pRefParams.get(BloomreachConstants.PRODUCT_FIELD.GENDER_TEXT)
					+ BloomreachConstants.QUOTES;
			String cat_id = pRefParams.get(BloomreachConstants.PRODUCT_FIELD.GENDER_TEXT)
					.replaceAll(BloomreachConstants.SINGLE_QUOTES, BloomreachConstants.EMPTY_STRING)
					.concat(SearchConstants.SPACE).concat(pRefParams.get(BloomreachConstants.PRODUCT_FIELD.WEB_PGC));
			constructParam.put(BloomreachConstants.CAT_ID, cat_id.trim());
			constructParam.put(BloomreachConstants.WEBSUBPGC + BloomreachConstants.QUOTES
					+ pRefParams.get(BloomreachConstants.PRODUCT_FIELD.WEB_SUB_PGC) + BloomreachConstants.QUOTES,
					BloomreachConstants.FILTER_FACET);
			if (null != pRefParams.get(BloomreachConstants.PRODUCT_FIELD.WEB_SUB_PGC) && !pRefParams
					.get(BloomreachConstants.PRODUCT_FIELD.WEB_SUB_PGC).equalsIgnoreCase(SearchConstants.SOCKS)) {
				constructParam.put(
						BloomreachConstants.BRAND_PARAMETER + BloomreachConstants.QUOTES
								+ pRefParams.get(BloomreachConstants.PRODUCT_FIELD.BRAND) + BloomreachConstants.QUOTES,
						BloomreachConstants.FILTER_FACET);
			}
			constructParam.put(BloomreachConstants.GENDER_TEXT + gender, BloomreachConstants.FILTER_FACET);
		}
		StringBuilder result = new StringBuilder();
		StringBuilder fieldFilter = new StringBuilder();
		for (Map.Entry<String, String> entry : constructParam.entrySet()) {
			String value = entry.getValue();
			try {
				if (!value.equalsIgnoreCase(BloomreachConstants.FILTER_FACET)) {
					result.append(URLEncoder.encode(entry.getKey(), BloomreachConstants.UTF_8));

					result.append(BloomreachConstants.EQUAL);
					if (!entry.getKey().equals(BloomreachConstants.UID_PARAM)
							&& !entry.getKey().equals(BloomreachConstants.SORT)) {
						value = URLEncoder.encode(entry.getValue(), BloomreachConstants.UTF_8);
					}
					result.append(value);
					result.append(BloomreachConstants.URL_DELIMETER);
				} else {
					fieldFilter.append(URLEncoder.encode(entry.getValue(), BloomreachConstants.UTF_8));
					fieldFilter.append(BloomreachConstants.EQUAL);
					fieldFilter.append(URLEncoder.encode(entry.getKey(), BloomreachConstants.UTF_8));
					fieldFilter.append(BloomreachConstants.URL_DELIMETER);
				}

			} catch (UnsupportedEncodingException unsupportedEncodingException) {
				log.error(
						"BloomreachSearchRecommendationServiceImpl::populatePathwaysRequestParam::unsupportedEncodingException={}",
						unsupportedEncodingException);
			}
		}
		result.append(fieldFilter.toString());
		log.debug("BloomreachSearchRecommendationServiceImpl :: populatePathwaysRequestParam :: END :: result: {}",
				result);
		return result.toString();
	}

	public List<RecommendationProductDTO> populateVIPProducts(Object profile,
			List<RecommendationProductDTO> upSellProducts) {
		String memberShipLevel = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
				.getHeader(BloomreachConstants.MEMBERSHIP_LEVEL);
		if (StringUtils.isEmpty(memberShipLevel)) {
			upSellProducts = addVIPProduct(upSellProducts, BloomreachConstants.RAC55);
		} else if (SearchConstants.VIP2.equalsIgnoreCase(memberShipLevel)) {
			upSellProducts = addVIPProduct(upSellProducts, BloomreachConstants.RAC100);
		} else if (SearchConstants.VIP3.equalsIgnoreCase(memberShipLevel)) {
			upSellProducts = addVIPProduct(upSellProducts, BloomreachConstants.RAC150);
		}
		log.info("BloomreachSearchRecommendationServiceImpl::populateVIPProducts::  upSellProducts {}", upSellProducts);
		return upSellProducts;
	}

	private List<RecommendationProductDTO> addVIPProduct(List<RecommendationProductDTO> recommendationProductList,
			String vipId) {
		DecimalFormat format = new DecimalFormat(SearchConstants.DEC_FORMAT);
		List<PriceDTO> priceDTOList = new ArrayList<>();
		if (!CollectionUtils.isEmpty(recommendationProductList)) {
			boolean listHasVipProduct = recommendationProductList.parallelStream()
					.anyMatch(searchProduct -> searchProduct.getSku().equalsIgnoreCase(vipId));
			if (listHasVipProduct) {
				return recommendationProductList;
			}
		}
		ProductDTO products = null;
		products = rrsProductRepository.getProducts(vipId);
		if (products != null) {
			String listPriceId = rrConfiguration.getRRSDefaultPriceListId();
			String salePriceId = rrConfiguration.getRRSSalePriceListId();
			String displayName = StringUtil.getStringFromObject(products.getDisplayName());
			String description = StringUtil.getStringFromObject(products.getDescription());
			RecommendationProductDTO productBean = new RecommendationProductDTO();
			productBean.setName(displayName);
			productBean.setDescription(description);
			String sku = StringUtil.getEncodedValue(StringUtil.getStringFromObject(products.getProductId()));
			productBean.setUrl(vipURLMap.get(vipId));
			productBean.setSku(sku);
			if (rrConfiguration.isEnableWidenImage() && nonPromotionalProducts.containsKey(sku)) {
				productBean.setImageId(nonPromotionalProducts.get(sku));
			}
			String imageUrlConstructed = productSkuHelper.getSkuImage(sku);
			productBean.setImageUrl(imageUrlConstructed);
			if (products.getCartOnlyClubPrice() != null) {
				boolean umapProduct = products.getCartOnlyClubPrice() == 1 ? true : false;
				productBean.setCartOnlyClubPrice(umapProduct);
			}
			double salePrice = 0.0, listPrice = 0.0;
			Double umap_price = 0.0;
			double umapPrice = 0.0;
			List<DCSProductChildSkus> childSkus = dcsProductSkusRepository.findByProductId(vipId);
			RRSSku rrsSKu = rrsSkuRepository.findBySkuId(vipId);
			if ((childSkus != null) && !childSkus.isEmpty()) {
				for (DCSProductChildSkus childSKU : childSkus) {
					salePrice = getDoubleSkuPrice(products, childSKU, salePriceId);
					listPrice = getDoubleSkuPrice(products, childSKU, listPriceId);
					productBean.setLowestListPrice(listPrice);
					umap_price = rrsSKu.getUmapPrice();
					umapPrice = umap_price != null ? (double) umapPrice : 0.0;
				}
			}
			double lowestListPrice = (double) listPrice;
			productBean.setLowestListPrice(lowestListPrice);
			productBean.setLowestSalePrice(salePrice);
			productBean.setHighestSalePrice(salePrice);
			boolean videoIcon = false;
			if (products.getVideoEmbeddedCode() != null && (products.getVideoEmbeddedCode().toString().length() > 1)) {
				videoIcon = true;
			}
			productBean.setDisplayVideo(videoIcon);
			String ratingId = SearchConstants.RRS + SearchConstants.MINUS + sku;
			RRSProductRating rrsRatings = rrsProductRatingRepository.findByRatingId(ratingId);
			if (rrsRatings != null) {
				float rating = (float) rrsRatings.getRating();
				int reviews = (int) rrsRatings.getReviews();
				productBean.setRating(rating);
				productBean.setReviews(reviews);
			}
			boolean umapHideVIP = false;
			if (products.getUmapHideVip() != null && products.getUmapHideVip() == 1) {
				umapHideVIP = true;
			}
			productBean.setUmapHideVIP(umapHideVIP);
			RRSProductWeb rrsProductWeb = productWebRepository.findByProductId(products.getProductId());
			String listPrices = String.valueOf(format.format(lowestListPrice));
			addPrice(priceDTOList, SearchConstants.PRICE_MSRP, listPrices, SearchConstants.STRING_ZERO);
			boolean exclusive = false;
			if (rrsProductWeb != null) {
				Integer vipExclusive = rrsProductWeb.getVipExclusive();
				if (vipExclusive != null && vipExclusive == 1) {
					exclusive = true;
				}
			}
			productBean.setExclusive(exclusive);
			if (!CollectionUtils.isEmpty(recommendationProductList)) {
				recommendationProductList.add(0, productBean);
				if (recommendationProductList.size() > upsellProductsSize) {
					recommendationProductList.remove(upsellProductsSize);
				}
			}
		}
		return recommendationProductList;
	}

	public Double getDoubleSkuPrice(ProductDTO product, DCSProductChildSkus sku, String priceList) {
		log.debug("BloomreachSearchRecommendationServiceImpl :: getDoubleSkuPrice() :: START");
		Double price = 0.0;
		DCSPrice dcsPrice = dcsPriceRepository.findBypriceList(priceList, product.getProductId(), sku.getSkuId());
		if (dcsPrice != null) {
			price = (Double) dcsPrice.getListPrice();
			log.debug("RelatedProductUtil :: getDoubleSkuPrice()==> price :: {} ", price);
			if (price == null) {
				price = 0.0;
				log.warn(
						"BloomreachSearchRecommendationServiceImpl :: getDoubleSkuPrice(): Price is not found for sku: ={}'",
						sku.getSkuId());
			}
		}
		log.debug("BloomreachSearchRecommendationServiceImpl :: getDoubleSkuPrice() :: END");
		return price;
	}

	private void addPrice(List<PriceDTO> priceDtos, String propertyName, String lowPriceValue, String highPriceValue) {
		log.debug(
				"BloomreachSearchRecommendationServiceImpl :: addPrice() :: START priceDtos {} propertyName {} lowPriceValue {} highPriceValue {}",
				priceDtos, propertyName, lowPriceValue, highPriceValue);
		if (StringUtils.isNotBlank(lowPriceValue)) {
			PriceDTO priceDTO = new PriceDTO();
			priceDTO.setLabel(propertyName);
			if (StringUtils.isNotBlank(highPriceValue) && Double.valueOf(highPriceValue) > 0.0
					&& Double.valueOf(highPriceValue) > Double.valueOf(lowPriceValue)) {
				priceDTO.setAmount(lowPriceValue + BloomreachConstants.HYPHEN + highPriceValue);
			} else {
				priceDTO.setAmount(lowPriceValue);
			}
			priceDTO.setType(propertyName);
			priceDTO.setSymbol(BloomreachConstants.DOLLER);
			priceDtos.add(priceDTO);
		}
		log.debug("BloomreachSearchRecommendationServiceImpl :: addPrice() :: END priceDtos {}", priceDtos);
	}

}
