package com.roadrunner.search.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
