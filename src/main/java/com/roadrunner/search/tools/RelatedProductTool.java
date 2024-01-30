package com.roadrunner.search.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.roadrunner.search.config.RRConfiguration;
import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.domain.RRSProductWeb;
import com.roadrunner.search.dto.ColorSkusDTO;
import com.roadrunner.search.dto.CrossSellProductsDTO;
import com.roadrunner.search.dto.ProductDTO;
import com.roadrunner.search.dto.RecommendationProductDTO;
import com.roadrunner.search.dto.RelatedProductResponseDTO;
import com.roadrunner.search.dto.UpSellProductsDTO;
import com.roadrunner.search.helper.CookieHelper;
import com.roadrunner.search.repo.RRSProductRepository;
import com.roadrunner.search.repo.RRSProductWebRepository;
import com.roadrunner.search.service.BloomreachSearchRecommendationService;

import jakarta.servlet.http.HttpServletRequest;
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
	private RRSProductRepository rrsProductRepository;

	@Autowired
	private RRSProductWebRepository productWebRepository;

	@Autowired
	private BloomreachSearchRecommendationService bloomreachSearchRecommendationService;

	@Autowired
	private CookieHelper cookieHelper;

	private Map<String, String> pgcSubcodeMap;
	private Map<String, String> webPgccodeMap;
	private String crossSellTitle;

	public RelatedProductResponseDTO generateRelatedProducts(String productId) {
		log.debug("RelatedProductTool::generateRelatedProducts:::START...productId: {}", productId);
		if (productId.isEmpty()) {
			log.error("RelatedProductTool::relatedProducts" + "::invalid request");
		}
		ProductDTO products = rrsProductRepository.getProducts(productId);
		RelatedProductResponseDTO relatedProductResponse = getRelatedProductResults(products, null);
		log.debug("RelatedProductTool::generateRelatedProducts::() relatedProductResponse:{}", relatedProductResponse);
		log.debug("RelatedProductTool::generateRelatedProducts:::END...");
		return relatedProductResponse;
	}

	private RelatedProductResponseDTO getRelatedProductResults(ProductDTO products, Object profile) {
		log.debug("RelatedProductTool :: getRelatedProductResults() :: STARTED");
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		RelatedProductResponseDTO relatedProductResponse = null;
		UpSellProductsDTO upSellProductsDTO = null;
		CrossSellProductsDTO crossSellProductsDTO = null;
		String webPgc = SearchConstants.EMPTY_STRING;
		String webSubPgc = SearchConstants.EMPTY_STRING;
		String brand = SearchConstants.EMPTY_STRING;
		String kidsGender = SearchConstants.EMPTY_STRING;
		Boolean outlet = SearchConstants.TRUE.equals(request.getParameter(SearchConstants.PARAM_OUTLET));
		boolean isWearometer = false;
		if (request.getParameter(SearchConstants.ISWEAROMETER) != null) {
			isWearometer = request.getParameter(SearchConstants.ISWEAROMETER).equalsIgnoreCase(SearchConstants.TRUE);
		}
		int sizeLimit = 0;
		Integer genderCode = null;
		sizeLimit = 12;
		if (null != outlet && outlet.booleanValue()) {
			relatedProductResponse = new RelatedProductResponseDTO();
			Map<String, String> refParams = new HashMap<String, String>();
			refParams.put(BloomreachConstants.PRODUCT_FIELD.OUTLET, SearchConstants.OUTLET);
			refParams.put(BloomreachConstants.PRODUCT_FIELD.RANKING, SearchConstants.RANKING);
			List<RecommendationProductDTO> upSellProducts = null;
			if (rrConfiguration.isEnableBloomreachSearch()) {
				refParams.put(BloomreachConstants.PRODUCT_FIELD.WEB_PGC, SearchConstants.SHOE);
				upSellProducts = bloomreachSearchRecommendationService.searchRecommendations(profile, refParams, false,
						false);
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
			upSellProducts = bloomreachSearchRecommendationService.searchRecommendationsForUpSell(profile, refParams,
					false, upSellProductsDTO);
			if (!CollectionUtils.isEmpty(upSellProducts)) {
				upSellProducts = upSellProducts.stream().limit(sizeLimit).collect(Collectors.toList());
			}
			upSellProductsDTO.setProducts(upSellProducts);
			upSellProductsDTO.setTitle(BloomreachConstants.BEST_SELLER_TITLE);
			relatedProductResponse.setUpsellProducts(upSellProductsDTO);
			refParams.put(BloomreachConstants.RECOMMENDATION_METHOD, BloomreachConstants.RECENTLY_VIEWED_PRODUCTS);
			refParams.put(BloomreachConstants.USER_ID, null);// user id should be passes
			CrossSellProductsDTO crossSell = new CrossSellProductsDTO();
			crossSellProducts = bloomreachSearchRecommendationService.searchRecommendationsForCrossSell(profile,
					refParams, false, crossSellProductsDTO);
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
						upSellProducts = bloomreachSearchRecommendationService.searchRecommendationsForUpSell(profile,
								refParams, false, upSellProductsDTO);
					} else {
						upSellProducts = bloomreachSearchRecommendationService.searchRecommendations(profile, refParams,
								false, false);
					}
					if (!CollectionUtils.isEmpty(upSellProducts)) {
						upSellProducts = upSellProducts.stream().limit(sizeLimit).collect(Collectors.toList());
					}
				}

				log.debug("ReleatedProductTool:: getRelatedProductResults :: refParams {}", refParams);
				boolean isHokaPage = false;
				if (!isWearometer) {
					upSellProductsDTO.setTitle(SearchConstants.UP_SELL_PRODUCTS_TITLE);
				} else {
					for (RecommendationProductDTO upsellProduct : upSellProducts) {
						upsellProduct.setRecommendedProduct(true);
					}
				}
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

			}

		}

		return relatedProductResponse;
	}

	private boolean isOutletProduct(String productId) {
		RRSProductWeb products = productWebRepository.findByProductId(productId);
		if (products == null) {
			log.debug("RelatedProductTool :: isOutletProduct :: null product arg products: {}", products);
			return false;
		}
		Boolean outlet = products.getClearance() != null && products.getClearance() == 1 ? true : false;
		return outlet;
	}

}
