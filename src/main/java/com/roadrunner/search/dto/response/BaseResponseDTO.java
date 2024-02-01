package com.roadrunner.search.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.roadrunner.search.dto.ErrorDetailDTO;

import lombok.Data;

@Data
public class BaseResponseDTO<T> {

	private T state;
	
	private List<ErrorDetailDTO> errors=new ArrayList<>();
	
	private boolean success;
	
}
