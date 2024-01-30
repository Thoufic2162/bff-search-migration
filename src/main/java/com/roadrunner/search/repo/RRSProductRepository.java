package com.roadrunner.search.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.roadrunner.search.domain.RRSProduct;
import com.roadrunner.search.dto.ProductDTO;

@Repository
public interface RRSProductRepository extends JpaRepository<RRSProduct, String> {

	@Query("select new com.roadrunner.search.dto.ProductDTO(r.productId,r.cartOnlyClubPrice,"
			+ "v.vendorName,r.gender,r.genderText,r.pgcSubCode,r.pgcCodeId,d.brand,d.displayName,d.description,r.videoEmbeddedCode,r.umapHideVip,r.defaultColor) "
			+ "from RRSProduct r,RRSVendor v,DCSProduct d WHERE r.productId= ?1 and r.vendorId=v.vendorId and r.productId=d.productId")
	ProductDTO getProducts(@Param("productId") String productId);

	RRSProduct findByProductId(String productId);
}
