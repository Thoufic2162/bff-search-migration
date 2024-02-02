package com.roadrunner.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryItemDTO {
	private String categoryName;
	private String refinements;
}
