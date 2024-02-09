package com.roadrunner.search.dto;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "catalogelementsfinder")
public class CatalogElementsFinder {
	private List<String> gender;
	private List<String> brands;
	private List<String> subCategory;
	private List<String> category;
	private List<String> size;
	private List<String> shoeType;
	private List<String> colors;
	private List<String> kidsGender;
	private List<String> kidsAccessoriesList;
	private List<String> nutritionSearchToCategoryList;
	private List<String> categoriesWithGear;
	private List<String> outletSubCategoryList;
	private Map<String, String> genderCategoryMap;
	private Map<String, String> apparelSearchToCategoryMap;
	private Map<String, String> shoesSearchToCategoryMap;
	private Map<String, String> pgcCategoryMap;
	private Map<String, String> accessoriesSubPgc;
	private Map<String, String> boysSearchToCategoryMap;
	private Map<String, String> girlsSearchToCategoryMap;
	private Map<String, String> equipmentSearchToCategoryMap;
	private Map<String, String> personalCareSearchToCategoryMap;
	private Map<String, String> womensApparelSubCategoryMap;
	private Map<String, String> mensApparelSubCategoryMap;
	private Map<String, String> searchQueryMap;
	private Map<String, String> apparelSizeMap;
	private Map<String, String> genderMap;
	private Map<String, String> bloomreachUrlQueryMap;
	private Map<String, String> bloomreachExcludeSearchMap;
	private Map<String, String> bloomreachBrandMap;
	private Map<String, String> queryMap;
	private Map<String, String> bloomreachDashBoardCategoryMap;
	private Map<String, String> bloomreachCategoryMap;
	private Map<String, String> sportsMap;
	private Map<String, String> webPgcMap;
}
