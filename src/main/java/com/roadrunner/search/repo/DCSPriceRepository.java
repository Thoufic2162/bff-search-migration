package com.roadrunner.search.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.roadrunner.search.domain.DCSPrice;

@Repository
public interface DCSPriceRepository extends JpaRepository<DCSPrice, String> {
	@Query("select d from DCSPrice d where d.priceList=?1 and (d.productId=?2 or d.skuId=null and (d.productId=null or d.skuId=?3))")
	DCSPrice findBypriceList(String priceList, String productId, String skuId);
}
