package com.roadrunner.search.helper.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.roadrunner.search.domain.BrandCategoryData;
import com.roadrunner.search.domain.DCSPrice;
import com.roadrunner.search.domain.DCSProductChildSkus;
import com.roadrunner.search.domain.RRSProductRating;
import com.roadrunner.search.domain.RRSProductWeb;
import com.roadrunner.search.domain.RRSSku;
import com.roadrunner.search.domain.SeoCategory;
import com.roadrunner.search.domain.SeoContent;
import com.roadrunner.search.dto.CategoryItemDTO;
import com.roadrunner.search.dto.ProductDTO;
import com.roadrunner.search.helper.ProductDataAccessHelper;
import com.roadrunner.search.repo.BrandCategoryDataRepository;
import com.roadrunner.search.repo.DCSPriceRepository;
import com.roadrunner.search.repo.DCSProductSkusRepository;
import com.roadrunner.search.repo.RRSCategoryMapRepository;
import com.roadrunner.search.repo.RRSProductRatingRepository;
import com.roadrunner.search.repo.RRSProductRepository;
import com.roadrunner.search.repo.RRSProductWebRepository;
import com.roadrunner.search.repo.RRSSkuRepository;
import com.roadrunner.search.repo.SeoCategoryRepository;
import com.roadrunner.search.repo.SeoContentRepository;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ProductDataAccessHelperImpl implements ProductDataAccessHelper {

	@Autowired
	private RRSCategoryMapRepository rrsCategoryMapRepository;

	@Autowired
	private SeoContentRepository seoContentRepository;

	@Autowired
	private SeoCategoryRepository seoCategoryRepository;

	@Autowired
	private RRSProductRatingRepository rrsProductRatingRepository;

	@Autowired
	private RRSProductRepository rrsProductRepository;

	@Autowired
	private RRSSkuRepository rrsSkuRepository;

	@Autowired
	private DCSPriceRepository dcsPriceRepository;

	@Autowired
	private DCSProductSkusRepository dcsProductSkusRepository;

	@Autowired
	private RRSProductWebRepository productWebRepository;

	@Autowired
	private BrandCategoryDataRepository brandCategoryDataRepository;

	@Value("${bloomreachsearchutil.siteId}")
	private long siteId;

	@Override
	public List<CategoryItemDTO> getCategoryItem(String query) {
		log.debug("ProductDataAccessHelperImpl :: getCategoryItem :: query: {}", query);
		List<CategoryItemDTO> itemList = null;
		if (StringUtils.isEmpty(query)) {
			return null;
		}
		itemList = rrsCategoryMapRepository.getCategoryItem(siteId, query);
		log.debug("ProductDataAccessHelperImpl :: getCategoryItem :: itemList: {}", itemList);
		return itemList;
	}

	@Override
	public SeoContent getSeoContent(String url) {
		log.debug("ProductDataAccessHelperImpl::getSeoContent() url:: {}", url);
		SeoContent seoContent = seoContentRepository.findBySeoUrl(url);
		return seoContent;
	}

	@Override
	public List<SeoCategory> getSeoCategory() {
		return seoCategoryRepository.getSeoCategory();
	}

	@Override
	public RRSProductRating getProductRating(String ratingId) {
		return rrsProductRatingRepository.findByRatingId(ratingId);
	}

	@Override
	public ProductDTO getProducts(String id) {
		return rrsProductRepository.getProducts(id);
	}

	@Override
	public RRSSku getProductSkus(String id) {
		return rrsSkuRepository.findBySkuId(id);
	}

	@Override
	public DCSPrice getProductPrice(ProductDTO product, DCSProductChildSkus sku, String priceList) {
		return dcsPriceRepository.findBypriceList(priceList, product.getProductId(), sku.getSkuId());
	}

	@Override
	public List<DCSProductChildSkus> getChildSkus(String id) {
		return dcsProductSkusRepository.findByProductId(id);
	}

	@Override
	public RRSProductWeb getProductData(String productId) {
		return productWebRepository.findByProductId(productId);
	}

	@Override
	public BrandCategoryData getBrandCategoryData(String brandName) {
		return brandCategoryDataRepository.findByBrandName(brandName);
	}

}
