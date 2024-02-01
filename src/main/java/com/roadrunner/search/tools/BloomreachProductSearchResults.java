package com.roadrunner.search.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.roadrunner.search.dto.BRDoc;
import com.roadrunner.search.dto.BloomreachSearchResponseDTO;
import com.roadrunner.search.dto.BloomreachSearchResultsDTO;
import com.roadrunner.search.dto.ColorSkusDTO;
import com.roadrunner.search.dto.PriceDTO;
import com.roadrunner.search.dto.RecommendationProductDTO;
import com.roadrunner.search.dto.Variants;
import com.roadrunner.search.helper.CookieHelper;
import com.roadrunner.search.helper.ProductPriceHelper;
import com.roadrunner.search.helper.ProductSkuHelper;
import com.roadrunner.search.service.BloomreachSearchService;
import com.roadrunner.search.util.StringUtil;
import com.roadrunner.search.util.URLCoderUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@ConfigurationProperties(prefix = "bloomreachproductsearchresults")
@Getter
@Setter
public class BloomreachProductSearchResults {

	private Map<String, String> productLandingSeoURL = new HashMap<String, String>();
	private Map<String, String> brandNameMap;
	private String vipPlus;
	private double vipPlusPrice;
	private List<String> promotionalProducts;

	@Autowired
	private RRConfiguration rrConfiguration;

	@Autowired
	private BloomreachSearchService bloomreachSearchService;

	@Autowired
	private ProductPriceHelper productPriceHelper;

	@Autowired
	private ProductSkuHelper productSkuHelper;

	@Autowired
	private CookieHelper cookieHelper;

	public void getProductResults(BloomreachSearchResponseDTO searchResult, HttpServletRequest request,
			BloomreachSearchResultsDTO responseBean) {
		log.debug(
				"BloomreachProductsearchResults :: getProductResults() :: START searchResult {} responseBean {} request {}",
				searchResult, responseBean, request);
		if (request == null) {
			request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		}
		List<RecommendationProductDTO> searchProductList = new ArrayList<>();
		if (searchResult != null && null != searchResult.getResponse()) {
			List<BRDoc> results = searchResult.getResponse().getDocs();
			boolean topPicks = false;
			if (request.getParameter(SearchConstants.TOP_PICKS_SECTION) != null) {
				topPicks = true;
			}
			if (topPicks && rrConfiguration.isEnableSockInTopPicks()) {
				BloomreachSearchResponseDTO bloomreachSearchResponse = bloomreachSearchService
						.populateBloomreachResponse(rrConfiguration.getTopPickSockProduct(), null);
				if (null != bloomreachSearchResponse.getResponse()
						&& !CollectionUtils.isEmpty(bloomreachSearchResponse.getResponse().getDocs())) {
					BRDoc product = bloomreachSearchResponse.getResponse().getDocs().get(0);
					results.add(1, product);
				}
			}
			if (null != results) {
				for (BRDoc result : results) {
					boolean isOutlet = false;
					boolean isCartOnlyClubPrice = false;
					boolean isVideoIcon = false;
					boolean isExclusive = false;
					RecommendationProductDTO searchProductDTO = new RecommendationProductDTO();
					String sku = result.getPid();
					String name = result.getTitle();
					String brand = result.getBrand();
					String gender = result.getGenderText();
					String exclusive = result.getExclusive();
					searchProductDTO.setSku(sku);
					searchProductDTO.setBrand(brand);
					searchProductDTO.setCategory(result.getSubCategory());
					if (null != result.getPgcCode()) {
						searchProductDTO.setPgccode(result.getPgcCode().get(0));
					}
					if (null != result.getOutlet() && result.getOutlet().equalsIgnoreCase(SearchConstants.OUTLET)) {
						isOutlet = true;
					}
					searchProductDTO.setOutlet(isOutlet);
					if (null != exclusive && exclusive.equalsIgnoreCase(BloomreachConstants.VIP_EXCLUSIVE)) {
						isExclusive = true;
					}
					searchProductDTO.setExclusive(isExclusive);
					if (!rrConfiguration.isEnableWidenImage()) {
						searchProductDTO.setImageUrl(result.getThumbImage());
					}
					String mapPricing = result.getMapPrice();
					if (SearchConstants.ONE_STRING.equals(mapPricing)) {
						isCartOnlyClubPrice = true;
					}
					searchProductDTO.setCartOnlyClubPrice(isCartOnlyClubPrice);
					searchProductDTO.setGender(gender);
					searchProductDTO.setDescription(result.getDescription());
					searchProductDTO.setRating(result.getCustomerRating());
					searchProductDTO.setReviews(result.getCustomerReviews());
					String videoIcon = result.getHasVideo();
					if (SearchConstants.ONE_STRING.equals(videoIcon)) {
						isVideoIcon = true;
					}
					searchProductDTO.setDisplayVideo(isVideoIcon);
					searchProductDTO.setName(name);
					String seoURl = getProductSeoUrl(sku, true, null, null, name);
					searchProductDTO.setUrl(seoURl);
					for (Map.Entry<String, String> entry : brandNameMap.entrySet()) {
						if (name.contains(entry.getKey())) {
							name = name.replaceAll(entry.getKey(), entry.getValue());
						}
					}
					double lowestListPrice = result.getRegPrice();
					if (sku.equals(vipPlus)) {
						searchProductDTO.setLowestListPrice(vipPlusPrice);
					} else {
						searchProductDTO.setLowestListPrice(lowestListPrice);
					}
					double lowestUmapPrice = getProductLowestUmapPrice(result);
					searchProductDTO.setLowestUmapPrice(lowestUmapPrice);

					double highestUmapPrice = getProductHighestUmapPrice(result);
					searchProductDTO.setHighestUmapPrice(highestUmapPrice);

					double lowestSalePrice = getProductLowestPrice(result, BloomreachConstants.SKU_FIELD.SALE_PRICE);
					searchProductDTO.setLowestSalePrice(lowestSalePrice);

					double highestSalePrice = getProductHighestPrice(result, BloomreachConstants.SKU_FIELD.SALE_PRICE,
							lowestListPrice);
					searchProductDTO.setHighestSalePrice(highestSalePrice);

					double lowestVIPPrice = getProductLowestPrice(result, BloomreachConstants.SKU_FIELD.VIP_PRICE);
					if (sku.equals(vipPlus)) {
						searchProductDTO.setLowestVIPPrice(vipPlusPrice);
					} else {
						searchProductDTO.setLowestVIPPrice(lowestVIPPrice);
					}

					double highestVIPPrice = getProductHighestPrice(result, BloomreachConstants.SKU_FIELD.VIP_PRICE,
							lowestListPrice);
					if (sku.equals(vipPlus)) {
						searchProductDTO.setHighestVIPPrice(vipPlusPrice);
					} else {
						searchProductDTO.setHighestVIPPrice(highestVIPPrice);
					}
					if (!StringUtils.isEmpty(result.getUmapHideVip())
							&& SearchConstants.ONE_STRING.equals(result.getUmapHideVip())) {
						searchProductDTO.setUmapHideVIP(true);
					} else {
						searchProductDTO.setUmapHideVIP(false);
					}
					// Setting Price for each product
					if (!promotionalProducts.contains(sku)) {
						productPriceHelper.setProdutPrices(searchProductDTO);
					}
					Map<String, Map<String, ColorSkusDTO>> skus = new HashMap<>();
					Map<String, ColorSkusDTO> skusDetais = productSkuHelper.createSkus(result, searchProductDTO);
					if (skusDetais.values().size() > 1) {
						searchProductDTO.setHasSkus(SearchConstants.TRUE);
						skus.put(sku, skusDetais);
					}
					boolean isRearrangedDefaultColor = productSkuHelper.rearrangeDefaultColor(request, searchProductDTO,
							result);
					if (null != result.getDefaultColor() && !isRearrangedDefaultColor) {
						searchProductDTO.setColorCode(result.getDefaultColor());
					} else if (!CollectionUtils.isEmpty(searchProductDTO.getColorsSkus())
							&& !isRearrangedDefaultColor) {
						String fstColorCode = searchProductDTO.getColorsSkus().stream().findFirst().get()
								.getColorCode();
						searchProductDTO.setColorCode(fstColorCode);
					}
					if (searchProductDTO.isDisplayVipMessage() && !CollectionUtils.isEmpty(searchProductDTO.getPrice())
							&& searchProductDTO.getPrice().stream()
									.anyMatch(price -> price.getType().equals(SearchConstants.VIP))) {
						searchProductDTO.getPrice().removeIf(price -> price.getType().equals(SearchConstants.VIP));
					}
					if (!CollectionUtils.isEmpty(searchProductDTO.getPrice())
							&& searchProductDTO.getPrice().stream()
									.anyMatch(price -> price.getType().equals(SearchConstants.VIP))
							&& brand != null && brand.equals(SearchConstants.GARMIN) && !isOutlet) {
						if (!searchProductDTO.getPrice().stream()
								.anyMatch(price -> price.getType().equals(SearchConstants.PRICE_MSRP))) {
							searchProductDTO.getPrice().stream()
									.filter(price -> price.getType().equals(SearchConstants.VIP)).forEach(price -> {
										price.setType(SearchConstants.PRICE_MSRP);
										price.setLabel(SearchConstants.PRICE_MSRP);
									});
						}
					}
					if (!CollectionUtils.isEmpty(searchProductDTO.getPrice())) {
						boolean hasVIPPrice = searchProductDTO.getPrice().stream()
								.anyMatch(price -> price.getType().equals(SearchConstants.VIP));
						boolean hasSalePrice = searchProductDTO.getPrice().stream()
								.anyMatch(price -> price.getType().equals(SearchConstants.PRICE_NOW));
						if (hasVIPPrice && hasSalePrice && brand != null && brand.equals(SearchConstants.GARMIN)) {
							// Find the VIP and Sale prices
							PriceDTO vipPrice = searchProductDTO.getPrice().stream()
									.filter(price -> price.getType().equals(SearchConstants.VIP)).findFirst()
									.orElse(null);
							PriceDTO salePrice = searchProductDTO.getPrice().stream()
									.filter(price -> price.getType().equals(SearchConstants.PRICE_NOW)).findFirst()
									.orElse(null);

							if (vipPrice != null && salePrice != null && vipPrice.getAmount() != null
									&& vipPrice.getAmount().equals(salePrice.getAmount())) {
								// Remove VIP prices if they match Sale prices
								searchProductDTO.getPrice()
										.removeIf(price -> price.getType().equals(SearchConstants.VIP));
							}
						}
					}
					if (!CollectionUtils.isEmpty(searchProductDTO.getPrice())
							&& searchProductDTO.getPrice().stream()
									.anyMatch(price -> price.getType().equals(SearchConstants.PRICE_NOW))
							&& brand != null && brand.equals(SearchConstants.GARMIN) && !isOutlet
							&& null != result.getSale()
							&& result.getSale().equalsIgnoreCase(BloomreachConstants.NOT_ON_SALE)) {
						searchProductDTO.getPrice()
								.removeIf(price -> price.getType().equals(SearchConstants.PRICE_NOW));
					}

					if (!CollectionUtils.isEmpty(searchProductDTO.getPrice())
							&& searchProductDTO.getPrice().stream()
									.anyMatch(price -> price.getType().equals(SearchConstants.VIP))
							&& searchProductDTO.getPrice().stream()
									.anyMatch(price -> price.getType().equals(SearchConstants.PRICE_MSRP))
							&& brand != null && brand.equals(SearchConstants.GARMIN)) {
						PriceDTO vipPrice = searchProductDTO.getPrice().stream()
								.filter(price -> price.getType().equals(SearchConstants.VIP)).findFirst().get();
						PriceDTO msrpPrice = searchProductDTO.getPrice().stream()
								.filter(price -> price.getType().equals(SearchConstants.PRICE_MSRP)).findFirst().get();
						if (vipPrice != null && msrpPrice != null && vipPrice.getAmount() != null
								&& vipPrice.getAmount() != null && vipPrice.getAmount().equals(msrpPrice.getAmount())) {
							searchProductDTO.getPrice().removeIf(price -> price.getType().equals(SearchConstants.VIP));
						}
					}
					if (!CollectionUtils.isEmpty(searchProductDTO.getPrice())
							&& searchProductDTO.getPrice().stream()
									.anyMatch(price -> price.getType().equals(SearchConstants.VIP))
							&& sku != null && sku.equals(SearchConstants.GIFTCARD)) {
						searchProductDTO.getPrice().removeIf(price -> price.getType().equals(SearchConstants.VIP));
					}
					String qUri = URLCoderUtil.decode(request.getParameter(SearchConstants.QURI));
					if (null != qUri && qUri.contains(SearchConstants.ALES_GREY)) {
						if (!searchProductDTO.getBrand().contains(SearchConstants.NIKE)) {
							searchProductList.add(searchProductDTO);
						}
					} else {
						if (topPicks && rrConfiguration.isEnableSockInTopPicks()
								&& sku.equals(rrConfiguration.getTopPickSockProduct())) {
							searchProductList.add(0, searchProductDTO);
						} else {
							searchProductList.add(searchProductDTO);
						}
					}
				}
			}
			cookieHelper.addCookies(request, searchProductList, responseBean);
		}
		if (null != searchResult && null != searchResult.getMetadata()) {
			responseBean.setMetaData(searchResult.getMetadata());
		}
		log.debug("BloomreachProductsearchResults :: getProductResults() :: END responseBean {}", responseBean);
		responseBean.setResults(searchProductList);
	}

	public String getProductSeoUrl(String productId, boolean constructUrl, String gender, String brand, String name) {
		String seoUrl = BloomreachConstants.EMPTY_STRING;
		if (productLandingSeoURL.containsKey(productId)) {
			seoUrl = productLandingSeoURL.get(productId);
		} else {
			if (constructUrl) {
				String url = SearchConstants.PDP_BASE_URL + productId + SearchConstants.SLASH;
				seoUrl = StringUtil.getSeoUrl(url, gender, brand, name);
			}
		}
		log.debug(
				"BloomreachProductsearchResults :: getProductSeoUrl() :: productId={}, constructUrl={}, gender={}, brand={}, name={}, seoUrl={}",
				productId, constructUrl, gender, brand, name, seoUrl);
		return seoUrl;
	}

	protected double getProductLowestUmapPrice(BRDoc result) {
		double res = 0.0;
		if (result == null) {
			log.debug("BloomreachProductsearchResults :: getProductLowestUmapPrice() :: Input params are empty");
			return res;
		}
		List<?> variants = (ArrayList<?>) result.getVariants();
		if (CollectionUtils.isEmpty(variants)) {
			log.debug("BloomreachProductsearchResults :: getProductLowestUmapPrice() :: No variants");
			return res;
		}
		Variants entries = null;
		Iterator<?> variantsIter = variants.iterator();
		List<Double> umapList = new ArrayList<Double>();
		List<Double> saleList = new ArrayList<Double>();
		double salePrice = 0;
		double umapPrice = 0;
		while (variantsIter.hasNext()) {
			entries = (Variants) variantsIter.next();
			if (entries == null) {
				continue;
			}

			if (null != entries.getUmapPrice()) {
				umapPrice = entries.getUmapPrice().get(0);
			}

			if (!CollectionUtils.isEmpty(entries.getRegPrice()) && entries.getRegPrice().size() > 0
					&& null != entries.getSkuSalePrice() && entries.getSkuSalePrice() < entries.getRegPrice().get(0)) {
				salePrice = entries.getSkuSalePrice();
			}
			if (umapPrice > 0) {
				umapList.add(Double.valueOf(umapPrice));
			}
			if (salePrice > 0) {
				saleList.add(Double.valueOf(salePrice));
			}
		}
		double umapRes = getRangeLowestPrice(umapList);
		double saleRes = getRangeLowestPrice(saleList);
		res = saleRes;
		if (umapRes > 0 && saleRes > 0 && umapRes > saleRes) {
			res = umapRes;
		}
		log.debug("BloomreachProductsearchResults :: getProductLowestUmapPrice() :: res: {}", res);
		return res;
	}

	public double getRangeLowestPrice(List<?> prices) {
		double price = 1000000.0;
		if (prices.isEmpty()) {
			return 0.0;
		}
		Iterator<?> iter = prices.iterator();
		Double itemPrice = null;
		while (iter.hasNext()) {
			itemPrice = (Double) iter.next();
			if (itemPrice != null && itemPrice.doubleValue() > 0) {
				price = Math.min(price, itemPrice.doubleValue());
			}
		}
		return (price != 1000000.0) ? price : 0.0;
	}

	protected double getProductHighestUmapPrice(BRDoc result) {
		double res = 0.0;
		log.debug("BloomreachProductsearchResults :: getProductHighestUmapPrice() :: START :: result {}", result);
		if (result == null) {
			log.debug("BloomreachProductsearchResults :: getProductHighestUmapPrice() :: Input params are empty");
			return res;
		}
		Variants entries = null;
		List<?> variants = (ArrayList<?>) result.getVariants();
		if (CollectionUtils.isEmpty(variants)) {
			log.debug("BloomreachProductsearchResults :: getProductHighestUmapPrice() :: No variants");
			return res;
		}
		Iterator<?> variantsIter = variants.iterator();
		List<Double> prices = new ArrayList<Double>();
		double regPrice = 0;
		double umapPrice = 0;
		double salePrice = 0;
		while (variantsIter.hasNext()) {
			entries = (Variants) variantsIter.next();
			if (entries == null) {
				continue;
			}
			if (null != entries.getUmapPrice()) {
				umapPrice = entries.getUmapPrice().get(0);
			}
			if (!CollectionUtils.isEmpty(entries.getRegPrice()) && entries.getRegPrice().size() > 0
					&& null != entries.getSkuSalePrice()) {
				salePrice = entries.getSkuSalePrice();
			}
			if (null != entries.getRegPrice()) {
				regPrice = entries.getRegPrice().get(0);
			}
			double umapPriceRes = 0;
			if (umapPrice > 0 && salePrice > 0) {
				if (umapPrice >= salePrice) {
					umapPriceRes = umapPrice;
				} else {
					umapPriceRes = salePrice;
				}
			} else {
				umapPriceRes = regPrice;
			}
			prices.add(Double.valueOf(umapPriceRes));

		}
		res = getRangeHighestPrice(prices);
		log.debug("BloomreachProductsearchResults :: getProductHighestUmapPrice() :: res: {} ", res);
		return res;
	}

	public static double getRangeHighestPrice(List<?> prices) {
		double price = 0.0;
		if (CollectionUtils.isEmpty(prices)) {
			return price;
		}
		Iterator<?> iter = prices.iterator();
		Double itemPrice = null;
		while (iter.hasNext()) {
			itemPrice = (Double) iter.next();
			if (itemPrice != null && itemPrice.doubleValue() > 0) {
				price = Math.max(price, itemPrice.doubleValue());
			}
		}
		return price;
	}

	protected double getProductLowestPrice(BRDoc result, String priceType) {
		double res = 0.0;
		log.debug("BloomreachProductsearchResults :: getProductLowestPrice() :: START :: result {} priceType {}",
				result, priceType);
		if (result == null) {
			log.debug("BloomreachProductsearchResults :: getProductLowestPrice() :: Input params are empty");
			return res;
		}
		Variants entries = null;
		List<?> variants = (ArrayList<?>) result.getVariants();
		if (CollectionUtils.isEmpty(variants)) {
			log.debug("BloomreachProductsearchResults :: getProductLowestPrice() :: No variants");
			return res;
		}

		Iterator<?> variantsIter = variants.iterator();
		List<Double> prices = new ArrayList<Double>();
		double skuPrice = 0;
		while (variantsIter.hasNext()) {
			entries = (Variants) variantsIter.next();
			if (entries == null) {
				continue;
			}
			if (priceType.equalsIgnoreCase(BloomreachConstants.SKU_FIELD.VIP_PRICE) && null != entries.getVipPrice()) {
				skuPrice = entries.getVipPrice().get(0);
			}
			if (!CollectionUtils.isEmpty(entries.getRegPrice()) && entries.getRegPrice().size() > 0
					&& null != entries.getSkuSalePrice() && entries.getSkuSalePrice() < entries.getRegPrice().get(0)
					&& priceType.equalsIgnoreCase(BloomreachConstants.SKU_FIELD.SALE_PRICE)) {
				skuPrice = entries.getSkuSalePrice();
			}
			if (skuPrice > 0) {
				prices.add(Double.valueOf(skuPrice));
			}
		}
		res = getRangeLowestPrice(prices);
		log.debug("BloomreachProductsearchResults :: getProductLowestPrice() :: res: {}", res);
		return res;
	}

	protected double getProductHighestPrice(BRDoc result, String priceType, double lowestListPrice) {
		double res = 0.0;
		log.debug(
				"BloomreachProductsearchResults :: getProductHighestPrice() :: START result {} priceType {} lowestListPrice {}",
				result, priceType, lowestListPrice);
		if (result == null) {
			log.debug("BloomreachProductsearchResults :: getProductHighestPrice() :: Input params are empty");
			return res;
		}
		Variants entries = null;
		List<?> variants = (ArrayList<?>) result.getVariants();
		if (CollectionUtils.isEmpty(variants)) {
			log.debug("BloomreachProductsearchResults :: getProductHighestPrice() :: No variants");
			return res;
		}

		Iterator<?> variantsIter = variants.iterator();
		List<Double> prices = new ArrayList<Double>();
		double skuPrice = 0;
		while (variantsIter.hasNext()) {
			entries = (Variants) variantsIter.next();
			if (entries == null) {
				continue;
			}
			if (priceType.equalsIgnoreCase(BloomreachConstants.SKU_FIELD.VIP_PRICE) && null != entries.getVipPrice()) {
				skuPrice = entries.getVipPrice().get(0);
			}
			if (!CollectionUtils.isEmpty(entries.getRegPrice()) && entries.getRegPrice().size() > 0
					&& null != entries.getSkuSalePrice() && entries.getSkuSalePrice() < entries.getRegPrice().get(0)
					&& priceType.equalsIgnoreCase(BloomreachConstants.SKU_FIELD.SALE_PRICE)) {
				skuPrice = entries.getSkuSalePrice();
			}
			if (skuPrice > 0) {
				prices.add(Double.valueOf(skuPrice));
			}
		}
		res = getRangeHighestPrice(prices);
		if (res > 0 && !CollectionUtils.isEmpty(variants) && prices.size() > 0 && prices.size() != variants.size()) {
			res = lowestListPrice;
		}
		log.debug("BloomreachProductsearchResults :: getProductHighestPrice() :: res: {} ", res);
		return res;
	}

}
