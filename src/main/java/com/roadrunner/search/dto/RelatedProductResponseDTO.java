package com.roadrunner.search.dto;

import lombok.Data;

@Data
public class RelatedProductResponseDTO {
	UpSellProductsDTO upsellProducts;

	CrossSellProductsDTO crossSellProducts;

}
