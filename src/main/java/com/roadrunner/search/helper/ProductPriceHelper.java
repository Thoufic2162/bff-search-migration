package com.roadrunner.search.helper;

import org.springframework.stereotype.Component;

import com.roadrunner.search.dto.RecommendationProductDTO;

@Component
public interface ProductPriceHelper {

	void setProdutPrices(RecommendationProductDTO searchProductDTO);
}
