package com.roadrunner.search.domain;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "RRS_WIDEN_ASSETS")
public class RRSWidenAssets {

	@Id
	@Column(name = "EMBED_ID")
	private String embedId;

	@Column(name = "STYLE_SKU")
	private String styleSku;

	@Column(name = "FILE_NAME")
	private String fileName;

	@Column(name = "IMAGE_TYPE")
	private String imageType;

	@Column(name = "CREATION_DATE")
	private Date creationDate;

	@Column(name = "LAST_MOD_DATE")
	private Date lastModifiedDate;

	@Column(name = "VERSION")
	private int version;
}
