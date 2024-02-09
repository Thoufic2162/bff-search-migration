package com.roadrunner.search.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "rrs_gbi_refinements")
@Data
public class RRSRefinements {
    @Id
    @Column(name = "CM_CATEGORY_MAP_ID")
    private String cmCategoryMapId;

    @Column(name = "SEQUENCE_NUM")
    private Long sequenceNum;

    @Column(name = "GBI_REFINEMENTS")
    private String gbiRefinements;
}
