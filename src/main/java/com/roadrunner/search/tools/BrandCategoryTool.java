package com.roadrunner.search.tools;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.roadrunner.search.config.RRConfiguration;
import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.constants.SearchConstants;
import com.roadrunner.search.domain.BrandCategoryData;
import com.roadrunner.search.dto.Banner1x6;
import com.roadrunner.search.dto.BrandCategoriesResponse;
import com.roadrunner.search.dto.BrandCategory;
import com.roadrunner.search.dto.Category;
import com.roadrunner.search.dto.LargeBrandCategories;
import com.roadrunner.search.dto.LargeBrandCategory;
import com.roadrunner.search.dto.LargeBrandDesktop;
import com.roadrunner.search.dto.LargeBrandFields;
import com.roadrunner.search.dto.LargeBrandFile;
import com.roadrunner.search.dto.LargeBrandImage;
import com.roadrunner.search.dto.LargeBrandItem;
import com.roadrunner.search.dto.LargeCarousel;
import com.roadrunner.search.dto.LargeDetails;
import com.roadrunner.search.dto.MediumBrandCategories;
import com.roadrunner.search.dto.MediumBrandDetails;
import com.roadrunner.search.dto.MediumBrandFields;
import com.roadrunner.search.dto.MediumBrandFile;
import com.roadrunner.search.dto.MediumBrandImage;
import com.roadrunner.search.dto.MediumSize;
import com.roadrunner.search.repo.BrandCategoryDataRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@ConfigurationProperties(prefix = "brandcategorytool")
@Getter
@Setter
public class BrandCategoryTool {

	@Autowired
	Gson gson;

	@Autowired
	private BrandCategoryDataRepository brandCategoryDataRepository;

	@Autowired
	private RRConfiguration rrConfiguration;

	private List<String> excludeBrand;
	private Map<String, String> brandImageUrlMap;
	private String imageUrl;
	private String apparelText;
	private List<String> ignoreSubCatagory;
	private Map<String, String> catMap;
	private Map<String, String> shoeOrderMap;
	private Map<String, String> apparelOrderMap;
	private List<String> excludeCarousel;

	public BrandCategoriesResponse getBrandData(String query) {
		log.debug("BrandCategoryTool::getBrandData:: START :: query {}", query);
		BrandCategoriesResponse brandCategoryResponse = null;
		BrandCategoryData repoData;
		repoData = getBrandApiFromDB(query);
		if (repoData != null) {
			Blob blob = repoData.getApiData();
			byte[] bdata = null;
			try {
				bdata = blob.getBytes(1, (int) blob.length());
			} catch (SQLException sqlException) {
				log.error("BrandCategoryTool::getBrandData:: sqlException{}", sqlException);
			}
			String apiData = new String(bdata);
			brandCategoryResponse = gson.fromJson(apiData, BrandCategoriesResponse.class);
			mapBrandCategoryValue(brandCategoryResponse);
		}
		log.debug("BrandCategoryTool::getBrandData::::Exception while getting data from DB for Brand::{} ", query);
		return brandCategoryResponse;
	}

	public BrandCategoryData getBrandApiFromDB(String brandName) {
		log.debug("BrandCategoryTool:::getBrandApiFromDB:::Searching for Brand:{}", brandName);
		if (null != brandName && !StringUtils.isEmpty(brandName)) {
			BrandCategoryData brandRepoItem = brandCategoryDataRepository.findByBrandName(brandName);
			if (brandRepoItem != null) {
				BrandCategoryData brandCategoryData = new BrandCategoryData();
				brandCategoryData.setBrandName((String) brandRepoItem.getBrandName());
				brandCategoryData.setApiData(brandRepoItem.getApiData());
				return brandCategoryData;
			}
		}
		log.debug("BrandCategoryTool:::getBrandApiFromDB:::Unable to Brand:{}", brandName);
		return null;
	}

	public void mapBrandCategoryValue(BrandCategoriesResponse brandCategoryResponse) {
		log.debug("BrandCategoryTool:::mapBrandCategoryValue:::START:{}");
		if (brandCategoryResponse != null && brandCategoryResponse.getBrandCategories() != null
				&& !brandCategoryResponse.getBrandCategories().isEmpty()) {
			String brandNameVal = brandCategoryResponse.getBrandName();
			brandCategoryResponse.setEnableDynamicBrand(true);
			if (excludeBrand != null && !excludeBrand.isEmpty() && excludeBrand.contains(brandNameVal)) {
				brandCategoryResponse.setEnableDynamicBrand(false);
			}
			if (brandCategoryResponse.getBrandType().equals(SearchConstants.LARGE_BRAND)) {
				String brandName = brandCategoryResponse.getBrandName();
				List<LargeBrandCategories> largeBrandCategories = new ArrayList<LargeBrandCategories>();
				brandCategoryResponse.getBrandCategories().stream().forEach(cat -> {
					String genderTitle = cat.getTitle();
					List<Category> catagories = cat.getCategories();
					if (null != catagories) {
						for (Category category : catagories) {
							if (!category.getTitle().equalsIgnoreCase(SearchConstants.GENDER_UNISEX)) {
								String imageMap = brandName.replace(SearchConstants.SPACE, SearchConstants.EMPTY_STRING)
										+ SearchConstants.UNDERSCORE
										+ genderTitle.replace(SearchConstants.APOSTROPHE, SearchConstants.EMPTY_STRING)
										+ SearchConstants.UNDERSCORE + category.getTitle();
								imageMap = imageMap.toLowerCase();
								if (!brandImageUrlMap.containsKey(imageMap)) {
									continue;
								}
								LargeBrandCategories largeBrand = new LargeBrandCategories();
								largeBrand.setSpacingStripe(40);
								String catName = brandName + SearchConstants.BRAND_WITH_SPACE + genderTitle
										+ SearchConstants.SPACE + category.getTitle();
								String shopAllTitle = SearchConstants.ALL + genderTitle + SearchConstants.SPACE
										+ category.getTitle();
								if (category.getTitle().equalsIgnoreCase(BloomreachConstants.SHOES)) {
									if (genderTitle.equalsIgnoreCase(BloomreachConstants.WOMENS)) {
										largeBrand.setOrder(1);
									} else {
										largeBrand.setOrder(2);
									}
								} else {
									if (genderTitle.equalsIgnoreCase(BloomreachConstants.WOMENS)) {
										largeBrand.setOrder(3);
									} else {
										largeBrand.setOrder(4);
									}
								}
								String title = genderTitle + SearchConstants.SPACE + category.getTitle();
								if (category.getTitle().equalsIgnoreCase((String) SearchConstants.APPAREL)) {
									title = genderTitle + SearchConstants.SPACE + apparelText;
								}
								largeBrand.setName(catName);
								LargeBrandCategory largeBrandCategory = new LargeBrandCategory();
								LargeBrandImage brandImage = new LargeBrandImage();
								LargeBrandFields fields = new LargeBrandFields();
								LargeBrandFields imageFields = new LargeBrandFields();
								LargeBrandFields desktopFields = new LargeBrandFields();
								LargeBrandDesktop desktop = new LargeBrandDesktop();
								LargeBrandFile desktopfile = new LargeBrandFile();
								LargeDetails details = new LargeDetails();
								LargeBrandImage imageFieldsSize = new LargeBrandImage();
								imageFields.setName(catName);
								desktopFields.setTitle(catName);
								desktopFields.setDescription(catName);
								desktopfile.setUrl(imageUrl + brandImageUrlMap.get(imageMap));
								details.setSize(51485);
								imageFieldsSize.setHeight(314);
								imageFieldsSize.setWidth(260);
								details.setImage(imageFieldsSize);
								desktopfile.setDetails(details);
								desktopFields.setFile(desktopfile);
								desktop.setFields(desktopFields);
								fields.setName(catName);
								brandImage.setFields(imageFields);
								fields.setImage(brandImage);
								fields.setTitle(title.toUpperCase());
								String categoryURL;
								if (category.getCategoryUrl().contains(SearchConstants.HOKA_ONE)) {
									categoryURL = category.getCategoryUrl().replace(SearchConstants.HOKA_ONE,
											SearchConstants.HOKA);
									fields.setLink(categoryURL);
									largeBrand.setShopAllLink(categoryURL);
								} else {
									fields.setLink(category.getCategoryUrl());
									largeBrand.setShopAllLink(category.getCategoryUrl());
								}
								fields.setTextAlign(SearchConstants.CENTER);
								imageFields.setDesktop(desktop);
								largeBrandCategory.setFields(fields);
								largeBrand.setCategory(largeBrandCategory);
								largeBrand.setShopAllText(SearchConstants.SHOP_ALL);
								largeBrand.setShopAllTitle(shopAllTitle);
								LargeCarousel carousel = new LargeCarousel();
								LargeBrandFields carouselFields = new LargeBrandFields();
								category.getSubCategories().stream().forEach(subCat -> {
									LargeBrandItem item = new LargeBrandItem();
									if (!ignoreSubCatagory.contains(subCat.getTitle().replace(SearchConstants.SPACE,
											SearchConstants.UNDERSCORE))) {
										LargeBrandFields itemFields = new LargeBrandFields();
										LargeBrandImage itemFieldImage = new LargeBrandImage();
										LargeBrandImage itemImage = new LargeBrandImage();
										LargeBrandFields itemImageField = new LargeBrandFields();
										LargeBrandDesktop desktopField = new LargeBrandDesktop();
										LargeBrandFields fieldDesktop = new LargeBrandFields();
										LargeBrandFile largeBrandFile = new LargeBrandFile();
										itemFields.setName(brandName + SearchConstants.SPACE + subCat.getTitle());
										itemImageField.setName(brandName);
										itemImageField.setMobileSize("375x425");
										itemImageField.setDesktop(desktopField);
										desktopField.setFields(fieldDesktop);
										fieldDesktop.setTitle(brandName + SearchConstants.SPACE + subCat.getTitle());
										fieldDesktop
												.setDescription(brandName + SearchConstants.SPACE + subCat.getTitle());
										fieldDesktop.setFile(largeBrandFile);
										if (null != subCat.getEmbedId() && rrConfiguration.isEnableWidenImage()) {
											largeBrandFile.setImageId(subCat.getEmbedId());
										} else if (null != subCat.getImageurl()) {
											largeBrandFile.setUrl(subCat.getImageurl()
													.replace(SearchConstants.SCENE7_IMAGE, SearchConstants.RRS_IMAGE));
										}
										LargeDetails filedetails = new LargeDetails();
										filedetails.setSize(25518);
										itemImage.setWidth(184);
										itemImage.setHeight(184);
										filedetails.setImage(itemImage);
										largeBrandFile.setDetails(filedetails);
										itemFieldImage.setFields(itemImageField);
										itemFields.setImage(itemFieldImage);
										if (catMap.containsKey(subCat.getTitle().replace(SearchConstants.SPACE,
												SearchConstants.UNDERSCORE))) {
											itemFields
													.setTitle(catMap
															.get(subCat.getTitle().replace(SearchConstants.SPACE,
																	SearchConstants.UNDERSCORE))
															.replace(SearchConstants.UNDERSCORE,
																	SearchConstants.SPACE));
										} else {
											itemFields.setTitle(subCat.getTitle());
										}
										String replaceItemFields;
										if (subCat.getCategoryUrl().contains(SearchConstants.HOKA_ONE)) {
											replaceItemFields = subCat.getCategoryUrl()
													.replace(SearchConstants.HOKA_ONE, SearchConstants.HOKA);
											itemFields.setLink(replaceItemFields);
										} else {
											itemFields.setLink(subCat.getCategoryUrl());
										}
										itemFields.setTextAlign(SearchConstants.CENTER);
										if (((String) SearchConstants.APPAREL).equalsIgnoreCase(category.getTitle())) {
											if (apparelOrderMap != null && apparelOrderMap.containsKey(
													itemFields.getTitle().toLowerCase().replace(SearchConstants.SPACE,
															SearchConstants.UNDERSCORE))) {
												String order = apparelOrderMap.get(itemFields.getTitle().toLowerCase()
														.replace(SearchConstants.SPACE, SearchConstants.UNDERSCORE));
												item.setOrder(Integer.parseInt(order));
											}
										}
										if ((SearchConstants.SHOES).equalsIgnoreCase(category.getTitle())) {
											if (shoeOrderMap != null
													&& shoeOrderMap.containsKey(subCat.getTitle().toLowerCase().replace(
															SearchConstants.SPACE, SearchConstants.UNDERSCORE))) {
												String order = shoeOrderMap.get(subCat.getTitle().toLowerCase()
														.replace(SearchConstants.SPACE, SearchConstants.UNDERSCORE));
												item.setOrder(Integer.parseInt(order));
											}
										}
										item.setFields(itemFields);
										if (!excludeCarousel.contains(largeBrand.getName())) {
											carouselFields.getItems().add(item);
										}
									}
								});
								carouselFields.getItems().sort(Comparator.comparing(o -> o.getOrder()));
								carouselFields.setSize(SearchConstants.MEDIUM);
								carouselFields.setTextAlign(SearchConstants.CENTER);
								carouselFields.setName(catName);
								carousel.setFields(carouselFields);
								largeBrand.setCarousel(carousel);
								largeBrandCategories.add(largeBrand);
							}
						}
					}
				});
				largeBrandCategories.sort(Comparator.comparing(o -> o.getOrder()));
				brandCategoryResponse.setLargeBrandCategories(largeBrandCategories);
			} else if (brandCategoryResponse.getBrandType().equals(SearchConstants.MEDIUM_BRAND)) {
				String brandName = brandCategoryResponse.getBrandName();
				MediumBrandCategories mediumBrandCategories = new MediumBrandCategories();
				AtomicInteger index = new AtomicInteger(1);
				BrandCategory cat = brandCategoryResponse.getBrandCategories().get(0);
				if (!cat.getTitle().equalsIgnoreCase(SearchConstants.GENDER_UNISEX)) {
					String genderTitle = cat.getTitle();
					List<Category> catagories = cat.getCategories();
					mediumBrandCategories.setName(brandName + SearchConstants.BRAND_CATEGORIES);
					mediumBrandCategories.setSpacingStripe(40);
					for (Category category : catagories) {
						category.getSubCategories().stream().forEach(subCat -> {
							String title = brandName + SearchConstants.SPACE + subCat.getTitle();
							String catName = brandName + SearchConstants.BRAND_WITH_SPACE + genderTitle
									+ SearchConstants.SPACE + subCat.getTitle();
							Banner1x6 banner = new Banner1x6();
							MediumSize size = new MediumSize();
							size.setId(index.getAndIncrement());
							banner.setSize(size);
							MediumBrandFields bannerField = new MediumBrandFields();
							MediumBrandFields imageField = new MediumBrandFields();
							bannerField.setName(title);
							bannerField.setTitle(title);
							bannerField.setLink(subCat.getCategoryUrl());
							bannerField.setTextAlign(SearchConstants.CENTER);
							MediumBrandImage fieldImage = new MediumBrandImage();
							MediumBrandImage fieldImageDet = new MediumBrandImage();
							MediumBrandFile fieldImageFile = new MediumBrandFile();
							MediumBrandDetails fieldImageDetails = new MediumBrandDetails();
							if (null != subCat.getEmbedId() && rrConfiguration.isEnableWidenImage()) {
								fieldImageFile.setImageId(subCat.getEmbedId());
							} else if (null != subCat.getImageurl()) {
								fieldImageFile.setUrl(subCat.getImageurl().replace(SearchConstants.SCENE7_IMAGE,
										SearchConstants.RRS_IMAGE));
							}
							fieldImageFile.setContentType(SearchConstants.IMAGE_JPEG);
							fieldImageDetails.setSize(6178);
							fieldImageDet.setWidth(184);
							fieldImageDet.setHeight(184);
							fieldImageDetails.setImage(fieldImageDet);
							fieldImageFile.setDetails(fieldImageDetails);
							imageField.setName(catName);
							imageField.setDescription(catName);
							imageField.setFile(fieldImageFile);
							fieldImage.setFields(imageField);
							bannerField.setImage(fieldImage);
							banner.setFields(bannerField);
							mediumBrandCategories.getBanner1x6().add(banner);
						});
					}
				}
				brandCategoryResponse.setMediumBrandCategories(mediumBrandCategories);
			}
		}
		log.debug("BrandCategoryTool:::mapBrandCategoryValue:::END:{}");
	}

}
