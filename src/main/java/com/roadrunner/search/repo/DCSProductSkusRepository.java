package com.roadrunner.search.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roadrunner.search.domain.DCSProductChildSkus;

@Repository
public interface DCSProductSkusRepository extends JpaRepository<DCSProductChildSkus, String> {
	List<DCSProductChildSkus> findByProductId(String productId);
}
