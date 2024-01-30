package com.roadrunner.search.helper;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.roadrunner.search.dto.BRDoc;
import com.roadrunner.search.dto.ColorSkusDTO;
import com.roadrunner.search.dto.RecommendationProductDTO;

import jakarta.servlet.http.HttpServletRequest;

@Component
public interface ProductSkuHelper {

	Map<String, ColorSkusDTO> createSkus(BRDoc result, RecommendationProductDTO searchProductDTO);

	boolean rearrangeDefaultColor(HttpServletRequest request, RecommendationProductDTO searchProductDTO, BRDoc result);

	String getSkuImage(String identifier);

}
