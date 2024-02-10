package com.roadrunner.search.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.roadrunner.search.domain.RRSWidenAssets;

@Repository
public interface RRSWidenAssetsRepository extends JpaRepository<RRSWidenAssets, String> {

	@Query("select r.embedId from RRSWidenAssets r where style_sku =:styleSku and r.imageType='MAIN' and r.fileName NOT LIKE '%_AMZ%'")
	String findEmbedIdByStyleSku(@Param("styleSku") String styleSku);

	@Query("select r.embedId from RRSWidenAssets r where style_sku like :styleSku and r.imageType='MAIN' and rownum=1 and r.fileName NOT LIKE '%_AMZ%'")
	String findEmbedIdByStyleSkuLike(@Param("styleSku") String styleSku);

}
