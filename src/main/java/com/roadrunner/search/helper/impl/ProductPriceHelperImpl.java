package com.roadrunner.search.helper.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.dto.PriceDTO;
import com.roadrunner.search.dto.RecommendationProductDTO;
import com.roadrunner.search.helper.ProductPriceHelper;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ProductPriceHelperImpl implements ProductPriceHelper {

	@Override
	public void setProdutPrices(RecommendationProductDTO searchProductDTO) {
		log.debug("ProductPriceHelperImpl :: setProdutPrices() :: START :: searchProductDTO {}", searchProductDTO);
		List<PriceDTO> priceDTOList = new ArrayList<>();
		DecimalFormat format = new DecimalFormat(SearchConstants.DEC_FORMAT);
		String saleMessage = null;
		String priceMsg = SearchConstants.PRICE_NOW;
		if (searchProductDTO.isOutlet()) {
			priceMsg = SearchConstants.PRICE_OUTLET;
		}
		log.debug("ProductPriceHelperImpl :: setProdutPrices() :: START :: searchProductDTO {}", searchProductDTO);
		boolean isRetailUser = false;
		if (isRetailUser) {// This logic is for checking the retail user from profile and setting the price
			addPrice(priceDTOList, SearchConstants.PRICE_MSRP);
		} else {
			String vipPriceMsg = SearchConstants.VIP;
			if (!searchProductDTO.isHideMsrp()) {
				double lowestListPrice = searchProductDTO.getLowestListPrice();
				String listPriceS = String.valueOf(format.format(lowestListPrice));
				addPrice(priceDTOList, SearchConstants.PRICE_MSRP, listPriceS, SearchConstants.STRING_ZERO);
			}
			if (searchProductDTO.isCartOnlyClubPrice() && searchProductDTO.getLowestListPrice() > 0) {
				if (searchProductDTO.getLowestUmapPrice() > 0
						&& searchProductDTO.getLowestUmapPrice() != searchProductDTO.getLowestListPrice()) {
					double lowestUmapPrice = searchProductDTO.getLowestUmapPrice();
					String lowestUmapPriceS = String.valueOf(format.format(lowestUmapPrice));
					double highestUmapPrice = searchProductDTO.getHighestUmapPrice();
					String highestUmapPriceS = String.valueOf(format.format(highestUmapPrice));
					addPrice(priceDTOList, priceMsg, lowestUmapPriceS, highestUmapPriceS);
				}
				if (!searchProductDTO.isUmapHideVIP() && searchProductDTO.getLowestVIPPrice() > 0
						&& searchProductDTO.getLowestUmapPrice() > 0
						&& searchProductDTO.getLowestUmapPrice() != searchProductDTO.getLowestListPrice()) {
					double lowestVIPPrice = searchProductDTO.getLowestVIPPrice();
					String lowestVIPPriceS = String.valueOf(format.format(lowestVIPPrice));
					double highestVIPPrice = searchProductDTO.getHighestVIPPrice();
					String highestVIPPriceS = String.valueOf(format.format(highestVIPPrice));
					addPrice(priceDTOList, vipPriceMsg, lowestVIPPriceS, highestVIPPriceS);
				}
				if (searchProductDTO.getLowestUmapPrice() > 0
						&& searchProductDTO.getLowestUmapPrice() != searchProductDTO.getLowestListPrice()
						&& searchProductDTO.getLowestSalePrice() > 0
						&& searchProductDTO.getLowestUmapPrice() > searchProductDTO.getLowestSalePrice()) {
					searchProductDTO.setDisplayVipMessage(true);
				} else if (!searchProductDTO.isUmapHideVIP()) {
					searchProductDTO.setDisplayVipMessage(true);
				}

			} else {
				if (searchProductDTO.getSpecialPricing() == SearchConstants.TRUE
						&& searchProductDTO.getLowestListPrice() > 0) {
					double lowestSalePrice = searchProductDTO.getLowestSalePrice();
					String lowestSalePriceS = String.valueOf(format.format(lowestSalePrice));
					double highestSalePrice = searchProductDTO.getHighestSalePrice();
					String highestSalePriceS = String.valueOf(format.format(highestSalePrice));

					addPrice(priceDTOList, SearchConstants.DAILY_SALE, lowestSalePriceS, highestSalePriceS);
				} else {
					if (searchProductDTO.getLowestSalePrice() > 0) {
						double lowestSalePrice = searchProductDTO.getLowestSalePrice();
						String lowestSalePriceS = String.valueOf(format.format(lowestSalePrice));
						double highestSalePrice = searchProductDTO.getHighestSalePrice();
						String highestSalePriceS = String.valueOf(format.format(highestSalePrice));
						addPrice(priceDTOList, priceMsg, lowestSalePriceS, highestSalePriceS);
					}
				}
				if (!searchProductDTO.isUmapHideVIP() && searchProductDTO.getLowestVIPPrice() > 0) {
					double lowestVIPPrice = searchProductDTO.getLowestVIPPrice();
					String lowestVIPPriceS = String.valueOf(format.format(lowestVIPPrice));
					double highestVIPPrice = searchProductDTO.getHighestVIPPrice();
					String highestVIPPriceS = String.valueOf(format.format(highestVIPPrice));
					addPrice(priceDTOList, vipPriceMsg, lowestVIPPriceS, highestVIPPriceS);
				}
			}
		}
		log.debug("ProductPriceHelperImpl :: setProdutPrices() :: priceDTOList{}", priceDTOList);
		if (!CollectionUtils.isEmpty(priceDTOList)) {
			searchProductDTO.setPrice(priceDTOList);
			searchProductDTO.setSaleMessage(saleMessage);
		}
		log.debug("ProductPriceHelperImpl :: setProdutPrices() :: END :: searchProductDTO {}", searchProductDTO);
	}

	private void addPrice(List<PriceDTO> priceDTOList, String propertyName, String lowPriceValue,
			String highPriceValue) {
		log.debug(
				"ProductPriceHelperImpl :: addPrice() :: START :: priceDTOList {} propertyName {} lowPriceValue {} highPriceValue {}",
				priceDTOList, propertyName, lowPriceValue, highPriceValue);
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
			priceDTOList.add(priceDTO);
		}
		log.debug("BloomreachProductSearchResults :: addPrice() :: END priceDTOList {}", priceDTOList);
	}

	private void addPrice(List<PriceDTO> priceDTOList, String propertyName) {
		log.debug("BloomreachProductSearchResults :: addPrice() :: START priceDTOList {} propertyName={}", priceDTOList,
				propertyName);
		PriceDTO priceDTO = new PriceDTO();
		priceDTO.setLabel(propertyName);
		priceDTO.setAmount(BloomreachConstants.EMPTY_STRING);
		priceDTO.setType(propertyName);
		priceDTO.setSymbol(BloomreachConstants.DOLLER);
		priceDTOList.add(priceDTO);
		log.debug("BloomreachProductSearchResults :: addPrice() :: END priceDTOList {}", priceDTOList);
	}

}
