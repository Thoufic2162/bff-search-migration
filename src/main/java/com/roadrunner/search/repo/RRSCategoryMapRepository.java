package com.roadrunner.search.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.roadrunner.search.domain.RRSCategoryMap;
import com.roadrunner.search.dto.CategoryItemDTO;

@Repository
public interface RRSCategoryMapRepository extends JpaRepository<RRSCategoryMap, String> {
	@Query("select new com.roadrunner.search.dto.CategoryItemDTO(c.categoryName,r.gbiRefinements) from RRSCategoryMap c ,"
			+ "RRSRefinements r WHERE c.siteId = :siteId AND c.keyName LIKE lower(CONCAT('%',:keyName,'%')) AND c.cmCategoryMapId= r.cmCategoryMapId")
	List<CategoryItemDTO> getCategoryItem(@Param("siteId") Integer siteId, @Param("keyName") String keyName);
}