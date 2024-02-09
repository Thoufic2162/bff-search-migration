package com.roadrunner.search.tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.roadrunner.search.config.RRConfiguration;
import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.constants.ErrorConstants;
import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.domain.RRSProductWeb;
import com.roadrunner.search.dto.BloomreachSearchResponseDTO;
import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.ColorSkusDTO;
import com.roadrunner.search.dto.CrossSellProductsDTO;
import com.roadrunner.search.dto.ErrorDetailDTO;
import com.roadrunner.search.dto.ProductDTO;
import com.roadrunner.search.dto.RecommendationProductDTO;
import com.roadrunner.search.dto.RelatedProductResponseDTO;
import com.roadrunner.search.dto.UpSellProductsDTO;
import com.roadrunner.search.dto.response.BaseResponseDTO;
import com.roadrunner.search.helper.CookieHelper;
import com.roadrunner.search.helper.ProductDataAccessHelper;
import com.roadrunner.search.service.BloomreachSearchRecommendationService;
import com.roadrunner.search.service.BloomreachSearchService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@ConfigurationProperties(prefix = "bloomreachsearchrecommendation")
@Getter
@Setter
public class RelatedProductTool {

	@Autowired
	private RRConfiguration rrConfiguration;

	@Autowired
	private BloomreachSearchRecommendationService bloomreachSearchRecommendationService;

	@Autowired
	private BloomreachProductSearchResults bloomreachProductSearchResults;

	@Autowired
	private BloomreachSearchService bloomreachSearchService;

	@Autowired
	private CookieHelper cookieHelper;

	@Autowired
	private ProductDataAccessHelper productDataAccessHelper;

	private Map<String, String> pgcSubcodeMap;
	private Map<String, String> webPgccodeMap;
	private String socks;
	private String insole;

	public BaseResponseDTO<RelatedProductResponseDTO> generateRelatedProducts(String productId, String page,
			HttpServletRequest request) {
		log.debug("RelatedProductTool::generateRelatedProducts:::START...productId: {}", productId);
		BaseResponseDTO<RelatedProductResponseDTO> response = new BaseResponseDTO<>();
		try {
			if (StringUtils.isEmpty(productId)) {
				log.error("RelatedProductTool::relatedProducts ::invalid request productId={}", productId);
			}
			ProductDTO products = productDataAccessHelper.getProducts(productId);
			if (products == null && page == null) {
				log.error("RelatedProductTool::relatedProducts::Cannot find the productId::product={}", productId);
				response.setSuccess(Boolean.FALSE);
				response.getErrors().add(new ErrorDetailDTO(new Date(), ErrorConstants.PRODUCT_NOT_FOUND));
			} else {
				RelatedProductResponseDTO relatedProductResponse = getRelatedProductResults(products, null, request);
				if (relatedProductResponse == null) {
					response.setSuccess(Boolean.FALSE);
					response.getErrors()
							.add(new ErrorDetailDTO(new Date(), ErrorConstants.FETCH_RELATED_PRODUCTS_ERROR));
				} else {
					response.setState(relatedProductResponse);
					response.setSuccess(Boolean.TRUE);
				}
			}
		} catch (Exception exception) {
			log.error("RelatedProductTool::generateRelatedProducts::() exception:{}", exception);
			response.setSuccess(Boolean.FALSE);
			response.getErrors().add(new ErrorDetailDTO(new Date(), exception.getMessage()));
		}
		log.debug("RelatedProductTool::generateRelatedProducts::() response:{}", response);
		log.debug("RelatedProductTool::generateRelatedProducts:::END...");
		return response;
	}

	public RelatedProductResponseDTO fetchNewOutletProducts(Object profile, HttpServletRequest request) {
		log.debug("RelatedProductTool::fetchNewOutletProducts::() generateNewOutletProducts:START request{}", request);
		Boolean outlet = (Boolean) request.getAttribute(SearchConstants.PARAM_OUTLET);
		int sizeLimit = 12;
		RelatedProductResponseDTO relatedProductResponse = null;
		UpSellProductsDTO upSellProductsDTO;
		if (null != outlet && outlet.booleanValue()) {
			relatedProductResponse = new RelatedProductResponseDTO();
			Map<String, String> refParams = new HashMap<String, String>();
			refParams.put(BloomreachConstants.PRODUCT_FIELD.OUTLET, SearchConstants.OUTLET);
			refParams.put(BloomreachConstants.PRODUCT_FIELD.RANKING, SearchConstants.RANKING);
			List<RecommendationProductDTO> upSellProducts = null;
			if (rrConfiguration.isEnableBloomreachSearch()) {
				refParams.put(BloomreachConstants.PRODUCT_FIELD.WEB_PGC, SearchConstants.SHOE);
				upSellProducts = bloomreachSearchRecommendationService.searchRecommendations(profile, refParams);
				if (upSellProducts != null)
					upSellProducts = upSellProducts.stream().limit(sizeLimit).collect(Collectors.toList());
			}
			upSellProductsDTO = new UpSellProductsDTO();
			upSellProductsDTO.setTitle(SearchConstants.NEW_TO_OUTLET_PRODUCTS_TITLE);
			if (!CollectionUtils.isEmpty(upSellProducts)) {
				upSellProductsDTO.setProducts(upSellProducts);
			}
			relatedProductResponse.setUpsellProducts(upSellProductsDTO);
		}
		log.debug(
				"RelatedProductTool::fetchNewOutletProducts::() generateNewOutletProducts:END relatedProductResponse{}",
				relatedProductResponse);
		return relatedProductResponse;
	}

	private RelatedProductResponseDTO getRelatedProductResults(ProductDTO products, Object profile,
			HttpServletRequest request) {
		log.debug("RelatedProductTool :: getRelatedProductResults() :: STARTED");
		RelatedProductResponseDTO relatedProductResponse = null;
		UpSellProductsDTO upSellProductsDTO = null;
		CrossSellProductsDTO crossSellProductsDTO = null;
		String webPgc = SearchConstants.EMPTY_STRING;
		String webSubPgc = SearchConstants.EMPTY_STRING;
		String brand = SearchConstants.EMPTY_STRING;
		Boolean outlet = SearchConstants.TRUE.equals(request.getAttribute(SearchConstants.PARAM_OUTLET));
		int sizeLimit = 0;
		Integer genderCode = null;
		sizeLimit = 12;
		// populating best seller products and recently viewed products for
		// no search result page
		String noResutlPage = request.getParameter(SearchConstants.PAGE);
		if (noResutlPage != null && noResutlPage.equalsIgnoreCase(SearchConstants.NO_RESULT_PAGE)) {
			relatedProductResponse = new RelatedProductResponseDTO();
			List<RecommendationProductDTO> upSellProducts = null;
			List<RecommendationProductDTO> crossSellProducts = null;
			Map<String, String> refParams = new HashMap<String, String>();
			upSellProductsDTO = new UpSellProductsDTO();
			refParams.put(BloomreachConstants.RECOMMENDATION_METHOD, BloomreachConstants.BEST_SELLER);
			upSellProducts = bloomreachSearchRecommendationService.searchRecommendationsForUpSellAndCrossSell(profile,
					refParams, upSellProductsDTO, null);
			if (!CollectionUtils.isEmpty(upSellProducts)) {
				upSellProducts = upSellProducts.stream().limit(sizeLimit).collect(Collectors.toList());
			}
			upSellProductsDTO.setProducts(upSellProducts);
			upSellProductsDTO.setTitle(BloomreachConstants.BEST_SELLER_TITLE);
			relatedProductResponse.setUpsellProducts(upSellProductsDTO);
			refParams.put(BloomreachConstants.RECOMMENDATION_METHOD, BloomreachConstants.RECENTLY_VIEWED_PRODUCTS);
			refParams.put(BloomreachConstants.USER_ID, SearchConstants.EMPTY_STRING);// user id should be passes
			CrossSellProductsDTO crossSell = new CrossSellProductsDTO();
			crossSellProducts = bloomreachSearchRecommendationService
					.searchRecommendationsForUpSellAndCrossSell(profile, refParams, null, crossSellProductsDTO);
			if (!CollectionUtils.isEmpty(crossSellProducts)) {
				crossSellProducts = crossSellProducts.stream().limit(sizeLimit).collect(Collectors.toList());
			}
			crossSell.setProducts(crossSellProducts);
			crossSell.setTitle(BloomreachConstants.RECENTLY_VIEWED_PRODUCTS_TITLE);
			relatedProductResponse.setCrossSellProducts(crossSell);
		}
		if (products != null) {
			relatedProductResponse = new RelatedProductResponseDTO();
			Map<String, String> refParams = new HashMap<String, String>();
			if (products.getVendorName() != null) {
				brand = products.getVendorName();
			}
			String gender = products.getGenderText();
			genderCode = products.getGender();
			if (!gender.isEmpty()) {
				refParams.put(BloomreachConstants.PRODUCT_FIELD.GENDER_TEXT, gender);
			}
			if (genderCode != null) {
				refParams.put(BloomreachConstants.PRODUCT_FIELD.GENDER, genderCode.toString());
			}
			if (!webPgc.isEmpty()) {
				refParams.put(BloomreachConstants.PRODUCT_FIELD.WEB_PGC, webPgc);
			}
			if (!webSubPgc.isEmpty()) {
				refParams.put(BloomreachConstants.PRODUCT_FIELD.WEB_SUB_PGC, webSubPgc);
			}
			if (!brand.isEmpty()) {
				refParams.put(BloomreachConstants.PRODUCT_FIELD.BRAND, brand);
			}
			if (webPgc.isEmpty()) {
				String pgcCode = products.getPgcCodeId();
				if (null != pgcCode) {
					webPgc = webPgccodeMap.get(pgcCode) != null ? webPgccodeMap.get(pgcCode) : pgcCode;
					refParams.put(BloomreachConstants.PRODUCT_FIELD.WEB_PGC, webPgc);
				}
			}
			if (webSubPgc.isEmpty()) {
				String subPgcCode = products.getPgcSubCode();
				if (null != subPgcCode) {
					webSubPgc = pgcSubcodeMap.get(subPgcCode) != null ? pgcSubcodeMap.get(subPgcCode) : subPgcCode;
					refParams.put(BloomreachConstants.PRODUCT_FIELD.WEB_SUB_PGC, webSubPgc);
				}
			}
			if (((!webPgc.contains(SearchConstants.PGC_CODE_VALUE) || webPgc.contains(SearchConstants.PGC_CODE_VALUE)
					&& webSubPgc.equalsIgnoreCase(SearchConstants.SOCKS))
					|| !webSubPgc.contains(BloomreachConstants.NUTRITION)) && noResutlPage == null) {
				upSellProductsDTO = new UpSellProductsDTO();
				log.debug("ReleatedProductStateDTOGenerator :: getRelatedProductResults :: refParams   ", refParams);
				List<RecommendationProductDTO> upSellProducts = null;
				if (rrConfiguration.isEnableBloomreachSearch()) {
					if (rrConfiguration.isEnablePathwayRecommendation()) {
						String productId = products.getProductId();
						if (!StringUtils.isEmpty(productId)) {
							refParams.put(BloomreachConstants.PRODUCT_FIELD.PRODUCT_ID, productId);
						}
						if (isOutletProduct(productId)) {
							refParams.put(BloomreachConstants.RECOMMENDATION_METHOD,
									BloomreachConstants.YOU_MAY_ALSO_LIKE_OUTLET);
						} else {
							refParams.put(BloomreachConstants.RECOMMENDATION_METHOD,
									BloomreachConstants.YOU_MAY_ALSO_LIKE);
						}
						upSellProducts = bloomreachSearchRecommendationService
								.searchRecommendationsForUpSellAndCrossSell(profile, refParams, upSellProductsDTO,
										null);
					}
					if (!CollectionUtils.isEmpty(upSellProducts)) {
						upSellProducts = upSellProducts.stream().limit(sizeLimit).collect(Collectors.toList());
					}
				}

				log.debug("ReleatedProductTool:: getRelatedProductResults :: refParams {}", refParams);
				boolean isHokaPage = false;
				upSellProductsDTO.setTitle(SearchConstants.UP_SELL_PRODUCTS_TITLE);
				if (!CollectionUtils.isEmpty(upSellProducts) && upSellProducts.size() > 0) {
					if (!CollectionUtils.isEmpty(upSellProducts)) {
						upSellProducts.stream().forEach(results -> {
							if (null != results.getColorsSkus() && results.getColorsSkus().size() > 1) {
								Optional<ColorSkusDTO> colorCode = results.getColorsSkus().stream()
										.filter(colorSkus -> !colorSkus.getColorDescription().toLowerCase()
												.contains(SearchConstants.BLACK))
										.findFirst();
								if (null != colorCode && colorCode.isPresent()) {
									results.setColorCode(colorCode.get().getColorCode());
								}
							}
						});
					}
					upSellProductsDTO.setProducts(upSellProducts);
					isHokaPage = upSellProducts.stream()
							.anyMatch(ishoka -> ishoka.getBrand().equalsIgnoreCase(SearchConstants.HOKA_UPPERCASE));
				}
				relatedProductResponse.setUpsellProducts(upSellProductsDTO);
				if (isHokaPage || null != brand && brand.equalsIgnoreCase(SearchConstants.HOKA_ONE_ONE)) {
					cookieHelper.hasHokaPage(true);
					cookieHelper.nonVipExceptHokaCookie(false, request);
				} else {
					cookieHelper.hasHokaPage(false);
					cookieHelper.nonVipExceptHokaCookie(true, request);
				}
				String page = request.getParameter(SearchConstants.PAGE);
				if (page != null && page.equalsIgnoreCase(SearchConstants.CART_PAGE)) {
					List<RecommendationProductDTO> crossSellProductsList = null;
					crossSellProductsDTO = new CrossSellProductsDTO();
					crossSellProductsDTO.setTitle(SearchConstants.CORSS_SELL_PRODUCTS_TITLE);
					if (outlet == null || !outlet) {
						refParams.put(BloomreachConstants.RECOMMENDATION_METHOD, BloomreachConstants.OUTFIT_YOUR_RUN);
					} else {
						refParams.put(BloomreachConstants.RECOMMENDATION_METHOD,
								BloomreachConstants.OUTFIT_YOUR_RUN_OUTLET);
					}
					crossSellProductsList = bloomreachSearchRecommendationService
							.searchRecommendationsForUpSellAndCrossSell(profile, refParams, null, crossSellProductsDTO);
					crossSellProductsDTO.setProducts(crossSellProductsList);
					relatedProductResponse.setCrossSellProducts(crossSellProductsDTO);
				} else {
					List<RecommendationProductDTO> crossSellProductsList = null;
					if (rrConfiguration.isEnablePathwayRecommendation() && webPgc != null
							&& !SearchConstants.APPAREL.equalsIgnoreCase(webPgc)) {
						crossSellProductsDTO = new CrossSellProductsDTO();
						crossSellProductsDTO.setTitle(SearchConstants.CORSS_SELL_PRODUCTS_TITLE);
						if (outlet == null || !outlet) {
							refParams.put(BloomreachConstants.RECOMMENDATION_METHOD,
									BloomreachConstants.OUTFIT_YOUR_RUN);
						} else {
							refParams.put(BloomreachConstants.RECOMMENDATION_METHOD,
									BloomreachConstants.OUTFIT_YOUR_RUN_OUTLET);
						}
						crossSellProductsList = bloomreachSearchRecommendationService
								.searchRecommendationsForUpSellAndCrossSell(profile, refParams, null,
										crossSellProductsDTO);
						crossSellProductsDTO.setProducts(crossSellProductsList);
						relatedProductResponse.setCrossSellProducts(crossSellProductsDTO);
					} else {
						refParams.put(SearchConstants.IS_APPAREL, SearchConstants.TRUE);
						List<String> recommendationIds = new ArrayList<String>();
						if (rrConfiguration.isEnableBloomreachSearch()) {
							recommendationIds = bloomreachSearchRecommendationService.searchRecommendation(refParams,
									products);
						}
						if (recommendationIds.contains(insole)) {
							int index = recommendationIds.indexOf(insole);
							recommendationIds.remove(index);
							recommendationIds.add(index, socks);
						}
						if (webPgc != null && BloomreachConstants.SHOES.equalsIgnoreCase(webPgc)) {
							crossSellProductsList = new ArrayList<>();
						} else {
							BloomreachSearchResponseDTO bloomreachSearchResponse = new BloomreachSearchResponseDTO();
							BloomreachSearchResultsDTO crossSellList = new BloomreachSearchResultsDTO();
							bloomreachSearchResponse = bloomreachSearchService.populateBloomreachResponse(null,
									recommendationIds);
							bloomreachProductSearchResults.getProductResults(bloomreachSearchResponse, null,
									crossSellList);
							crossSellProductsList = crossSellList.getResults();
							crossSellProductsList = (crossSellProductsList != null) ? crossSellProductsList.stream()
									.sorted(Comparator.comparing(sort -> ((RecommendationProductDTO) sort).getSku(),
											Comparator.comparingInt(recommendationIds::indexOf)))
									.collect(Collectors.toList()) : null;
						}
						int limit = 14;
						if (webPgc != null && BloomreachConstants.SHOES.equalsIgnoreCase(webPgc)) {
							limit = 24;
						}
						if (null != crossSellProductsList) {
							crossSellProductsList = crossSellProductsList.stream().limit(limit)
									.collect(Collectors.toList());
						}
						if (crossSellProductsList != null && !CollectionUtils.isEmpty(crossSellProductsList)
								&& crossSellProductsList.size() > 1) {
							crossSellProductsDTO = new CrossSellProductsDTO();
							crossSellProductsDTO.setTitle(SearchConstants.CORSS_SELL_PRODUCTS_TITLE);
							if (crossSellProductsList.stream()
									.anyMatch(prod -> prod.getSku().contains(SearchConstants.RAC))) {
								crossSellProductsList.removeIf(prod -> prod.getSku().contains(SearchConstants.RAC));
							}
							if (!CollectionUtils.isEmpty(crossSellProductsList)) {
								crossSellProductsList.stream().forEach(results -> {
									if (null != results.getColorsSkus() && results.getColorsSkus().size() > 1) {
										Optional<ColorSkusDTO> colorCode = results.getColorsSkus().stream()
												.filter(colorSkus -> !colorSkus.getColorDescription().toLowerCase()
														.contains(SearchConstants.BLACK))
												.findFirst();
										if (null != colorCode && colorCode.isPresent()) {
											results.setColorCode(colorCode.get().getColorCode());
										}
									}
								});
							}
							crossSellProductsDTO.setProducts(crossSellProductsList);
							relatedProductResponse.setCrossSellProducts(crossSellProductsDTO);
						}
					}
				}
			}
		}
		return relatedProductResponse;
	}

	private boolean isOutletProduct(String productId) {
		RRSProductWeb products = productDataAccessHelper.getProductData(productId);
		if (products == null) {
			log.debug("RelatedProductTool :: isOutletProduct :: null product arg products: {}", products);
			return false;
		}
		return products.getClearance() != null && products.getClearance() == 1 ? Boolean.TRUE : Boolean.FALSE;
	}

}
