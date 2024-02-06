package com.roadrunner.search.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = " rrs_seo_content_data")
@Component
@Getter
@Setter
public class SeoContent {

	@Id
	@Column(name = "seo_url")
	private String seoUrl;

	@Column(name = "PAGE_TITLE")
	private String pageTitle;

	@Column(name = "CANONICAL_URL")
	private String canonicalUrl;

	@Column(name = "SEO_TITLE")
	private String seoTitle;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "KEYWORDS")
	private String keywords;

	@Column(name = "H1")
	private String h1;

	@Column(name = "H2")
	private String h2;

	@Column(name = "H3")
	private String h3;

	@Column(name = "FOOTER_CONTENT")
	private String footerContent;

	@Column(name = "HEADER_CONTENT")
	private String headerContent;

	@Column(name = "REDIRECT_URL")
	private String redirectUrl;

	@Column(name = "FOOTER_CONTENT_FAQ")
	private String footerContentFaq;

	@Column(name = "CUSTOM_URL")
	private String customUrl;

	@Column(name = "BANNER_CONTENT")
	private String bannerContent;

	@Column(name = "BREAD_CRUMBS")
	private String breadCrums;
}
