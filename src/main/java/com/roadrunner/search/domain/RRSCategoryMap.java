package com.roadrunner.search.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "rrs_cm_category_map")
@Data
public class RRSCategoryMap {
	@Id
	@Column(name = "CM_CATEGORY_MAP_ID")
	private String cmCategoryMapId;

	@Column(name = "KEY_NAME")
	private String keyName;

	@Column(name = "CATEGORY_NAME")
	private String categoryName;

	@Column(name = "TITLE")
	private String title;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "KEYWORDS")
	private String keywords;

	@Column(name = "CANONICALURL")
	private String canonicalUrl;

	@Column(name = "PAGE_NAME")
	private String pageName;

	@Column(name = "SOCIAL_IMAGE_PATH")
	private String socialImagePath;

	@Column(name = "QUICK_LINKS")
	private String quickLinks;

	@Column(name = "GBI_SORT")
	private String gbiSort;

	@Column(name = "GBI_QUERY")
	private String gbiQuery;

	@Column(name = "SITE_ID")
	private Long siteId;

	@Column(name = "GBI_PRUNEREF")
	private Integer gbiPruneref;

	@Column(name = "H1")
	private String h1;
}
