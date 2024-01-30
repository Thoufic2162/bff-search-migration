package com.roadrunner.search.helper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.roadrunner.search.config.BloomreachConfiguration;
import com.roadrunner.search.config.RRConfiguration;
import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.dto.BRDoc;
import com.roadrunner.search.dto.CatalogElementsFinder;
import com.roadrunner.search.dto.ColorSkusDTO;
import com.roadrunner.search.dto.InventoryDTO;
import com.roadrunner.search.dto.RecommendationProductDTO;
import com.roadrunner.search.dto.Variants;
import com.roadrunner.search.helper.ProductSkuHelper;
import com.roadrunner.search.helper.SearchHelper;
import com.roadrunner.search.repo.SeoContentRepository;
import com.roadrunner.search.util.HttpUtil;
import com.roadrunner.search.util.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@SuppressWarnings({ "unused" })
@ConfigurationProperties(prefix = "bloomreachproductsearchresults")
@Getter
@Setter
public class ProductSkuHelperImpl implements ProductSkuHelper {

	@Autowired
	private RRConfiguration rrConfiguration;

	private Map<String, String> colorCodeMap;

	@Override
	public Map<String, ColorSkusDTO> createSkus(BRDoc result, RecommendationProductDTO searchProductDTO) {
		log.debug("ProductSkuHelperImpl :: createSkus() :: START createSkus ::", result);
		Map<String, ColorSkusDTO> colorSkus = new HashMap<>();
		if (result == null || searchProductDTO == null) {
			log.debug("ProductSkuHelperImpl :: createSkus() :: Input params are empty");
			return colorSkus;
		}
		String colorCode = null;
		String skuId = null;
		String styleSku = searchProductDTO.getSku();
		ColorSkusDTO colorSkusDTO = null;
		Variants entries = null;
		List<?> variants = (ArrayList<?>) result.getVariants();
		if (CollectionUtils.isEmpty(variants)) {
			log.debug("ProductSkuHelperImpl :: createSkus() :: No variants");
			return colorSkus;
		}

		Iterator<?> variantsIter = variants.iterator();
		int productStock = 0;
		InventoryDTO inventoryDTO = null;
		String style = null;
		while (variantsIter.hasNext()) {
			entries = (Variants) variantsIter.next();
			if (entries == null) {
				continue;
			}
			colorSkusDTO = new ColorSkusDTO();
			skuId = entries.getSkuid();
			skuId = StringUtil.getEncodedValue(skuId);
			colorSkusDTO.setSku(skuId);
			// color/color code
			String skuColorCode = null;
			String refColorCode = null;
			String stylesSku = null;
			if (null != entries.getRefColorCode()) {
				refColorCode = entries.getRefColorCode().get(0);
			}
			String flavor = null;
			if (null != entries.getFlavorValue()) {
				flavor = entries.getFlavorValue().get(0);
			}
			String color = entries.getSkuColor();

			StringTokenizer tokenizer = new StringTokenizer(skuId, BloomreachConstants.HYPHEN);
			if (tokenizer.countTokens() > 1) {
				style = tokenizer.nextToken(); // style
				skuColorCode = tokenizer.nextToken();
				colorCode = (!StringUtils.isEmpty(refColorCode)) ? refColorCode : skuColorCode;
				if (isColorCode(color, flavor) || styleSku.equalsIgnoreCase(SearchConstants.GIFTCARD)) {
					colorSkusDTO.setColorCode(colorCode);
				}
			} else if (tokenizer.countTokens() == 1) {
				style = styleSku;
				skuColorCode = SearchConstants.EMPTY_STRING;
			}

			String imageUrlConstructed = getSkuImage(styleSku + BloomreachConstants.HYPHEN + colorCode);
			colorSkusDTO.setImageUrl(imageUrlConstructed);

			boolean mainFeature = false;
			String mainFeatureKey = colorSkusDTO.getColorCode();
			if (!StringUtils.isEmpty(color)) {
				colorSkusDTO.setColorDescription(color);
				mainFeature = true;
			}
			if (!mainFeature && !StringUtils.isEmpty(flavor)) {
				colorSkusDTO.setColorDescription(flavor);
				mainFeature = true;
			}

			if (styleSku.equalsIgnoreCase(SearchConstants.GIFTCARD)) {
				colorSkusDTO.setColorCode(SearchConstants.DOLLAR_STR.concat(colorSkusDTO.getColorCode()));
				mainFeature = true;
			}
			if (null != skuId) {
				mainFeature = true;
			}

			String qohS = null;
			if (null != entries.getQuantityOnHand()) {
				qohS = entries.getQuantityOnHand().get(0).replaceAll(SearchConstants.DOUBLE_REGEX,
						SearchConstants.EMPTY_STRING);
			}
			if (!StringUtils.isEmpty(qohS)) {
				colorSkusDTO.setQuantityOnHand(qohS);
			}
			stylesSku = style + BloomreachConstants.HYPHEN + skuColorCode;
			tokenizer = new StringTokenizer(skuId, BloomreachConstants.HYPHEN);
			if (tokenizer.countTokens() == 1) {
				stylesSku = style;
			}
			final String retrieveSku = stylesSku;
			if (rrConfiguration.isEnableWidenImage()) {
				Optional<Variants> embedId = result.getVariants().stream()
						.filter(skuStyle -> skuStyle.getSkuid().contains(retrieveSku)
								&& !CollectionUtils.isEmpty(skuStyle.getEmbedId())
								&& !skuStyle.getEmbedId().get(0).contains(BloomreachConstants.EMPTY_EMBED_ID))
						.findFirst();

				if (null != embedId && embedId.isPresent()
						&& !embedId.get().getEmbedId().get(0).equalsIgnoreCase(BloomreachConstants.COMMA)) {
					colorSkusDTO.setEmbedId(embedId.get().getEmbedId().get(0));
				}
				if (rrConfiguration.isEnableAltImagesInPlp()) {
					Optional<Variants> altEmbedId = result.getVariants().stream()
							.filter(skuStyle -> skuStyle.getSkuid().contains(retrieveSku)
									&& !CollectionUtils.isEmpty(skuStyle.getAltEmbedId())
									&& !skuStyle.getAltEmbedId().get(0).contains(BloomreachConstants.EMPTY_EMBED_ID))
							.findFirst();

					if (null != altEmbedId && altEmbedId.isPresent()
							&& !altEmbedId.get().getAltEmbedId().get(0).equalsIgnoreCase(BloomreachConstants.COMMA)) {
						colorSkusDTO.setAltEmbedId(embedId.get().getAltEmbedId().get(0));
					}
				}
			}

			if (mainFeature) {
				searchProductDTO.setColorsSku(colorSkusDTO);
				colorSkus.put(mainFeatureKey, colorSkusDTO);
			}
			if (StringUtils.isNotEmpty(qohS)) {
				productStock += Integer.valueOf(qohS);
			}
		}

		if (productStock > 0) {
			inventoryDTO = new InventoryDTO();
			inventoryDTO.setInventoryMessage(SearchConstants.INSTOCK_MSG);
			inventoryDTO.setStockLevel(productStock);
			inventoryDTO.setAvailabilityStatus(SearchConstants.INSTOCK_CODE);
			searchProductDTO.setInventory(inventoryDTO);
		}
		log.debug("ProductSkuHelperImpl :: doSearch() END :: colorSkus {}", colorSkus);
		return colorSkus;
	}

	@Override
	public boolean rearrangeDefaultColor(HttpServletRequest request, RecommendationProductDTO searchProductDTO,
			BRDoc result) {
		log.debug("ProductSkuHelperImpl :: rearrangeDefaultColor :: START request {} searchProductDTO {} result{}",
				request, searchProductDTO, result);
		String rParam = null;
		Properties queryParams = null;
		boolean isRearrangedColor = false;
		Optional<ColorSkusDTO> colorCode = null;
		Optional<ColorSkusDTO> colorData = null;
		if (request != null) {
			queryParams = HttpUtil.getRequestParams(request);
			if (null != queryParams && null != queryParams.get(BloomreachConstants.QPARAMS.R)) {
				rParam = queryParams.get(BloomreachConstants.QPARAMS.R).toString();
			}
		}
		if (null == result.getDefaultColor() && null != queryParams
				&& null == queryParams.get(SearchConstants.SELECTED_COLOR)) {
			colorCode = searchProductDTO.getColorsSkus().stream()
					.filter(numeric -> NumberUtils.isParsable(numeric.getColorCode()))
					.filter(colorSkus -> Integer.parseInt(colorSkus.getColorCode()) > 20).findFirst();
		}

		if ((null != rParam && rParam.contains(BloomreachConstants.SKU_FIELD.VARIANTS_COLOR_GROUP)
				&& null != queryParams && null != queryParams.get(SearchConstants.SELECTED_COLOR))
				|| (null != queryParams && null != queryParams.get(SearchConstants.SEARCH_COLOR))) {
			String selColor = null != queryParams.get(SearchConstants.SELECTED_COLOR)
					? queryParams.get(SearchConstants.SELECTED_COLOR).toString()
					: (String) queryParams.get(SearchConstants.SEARCH_COLOR);
			String[] colors = selColor.split(BloomreachConstants.COMMA);
			for (String color : colors) {
				String colorCodeData = colorCodeMap.get(color);
				if (StringUtils.isNotEmpty(colorCodeData)) {
					String split[] = colorCodeData.split(BloomreachConstants.HYPHEN);
					int colorCodeBegin = Integer.parseInt(split[0]);
					int colorCodeEnd = Integer.parseInt(split[1]);
					colorCode = searchProductDTO.getColorsSkus().stream()
							.filter(numeric -> NumberUtils.isParsable(numeric.getColorCode()))
							.filter(colorSkus -> Integer.parseInt(colorSkus.getColorCode()) >= colorCodeBegin
									&& Integer.parseInt(colorSkus.getColorCode()) <= colorCodeEnd)
							.findFirst();
					colorData = colorCode;
				}
				if (null != result.getVariants() && !colorData.isPresent()) {
					AtomicReference<String> defaultColorCode = new AtomicReference<>();
					result.getVariants().stream().filter(selcolor -> null != selcolor.getColorGroup()
							&& selcolor.getColorGroup().get(0).equalsIgnoreCase(color)).forEach(clrCode -> {
								StringTokenizer tokenizer = new StringTokenizer(clrCode.getSkuid(),
										BloomreachConstants.HYPHEN);
								if (tokenizer.countTokens() > 1) {
									String style = tokenizer.nextToken(); // style
									String skuColorCode = tokenizer.nextToken();
									defaultColorCode.set(skuColorCode);
								}
							});
					if (null != defaultColorCode.get()) {
						searchProductDTO.setColorCode(defaultColorCode.get());
						isRearrangedColor = true;
					}
				}
				if (null != colorCode && colorCode.isPresent()) {
					break;
				}
			}
			isRearrangedColor = true;
		}
		if (null != colorCode && colorCode.isPresent()) {
			searchProductDTO.setColorCode(colorCode.get().getColorCode());
			isRearrangedColor = true;
		}
		log.debug("ProductSkuHelperImpl::rearrangeDefaultColor END isRearrangedColor={}", isRearrangedColor);
		return isRearrangedColor;
	}

	protected boolean isColorCode(String color, String flavor) {
		return !StringUtils.isEmpty(color) || !StringUtils.isEmpty(flavor);
	}

	@Override
	public String getSkuImage(String identifier) {
		String imageUrlConstructed = "http://s7ondemand1.scene7.com/is/image/roadrunnersports/" + identifier
				+ "?$productListRRS$";
		if (!rrConfiguration.isEnableWidenImage()) {
			return imageUrlConstructed;
		}
		return null;
	}

}
