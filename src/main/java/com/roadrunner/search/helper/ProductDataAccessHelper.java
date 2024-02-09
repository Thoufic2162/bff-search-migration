package com.roadrunner.search.helper;

import java.util.List;

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

@Component
public interface ProductDataAccessHelper {

	List<CategoryItemDTO> getCategoryItem(String query);

	SeoContent getSeoContent(String url);

	List<SeoCategory> getSeoCategory();

	RRSProductRating getProductRating(String ratingId);

	ProductDTO getProducts(String id);

	RRSSku getProductSkus(String id);

	DCSPrice getProductPrice(ProductDTO product, DCSProductChildSkus sku, String priceList);

	List<DCSProductChildSkus> getChildSkus(String id);

	RRSProductWeb getProductData(String productId);

	BrandCategoryData getBrandCategoryData(String brandName);

}
