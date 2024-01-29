package com.roadrunner.search.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.roadrunner.search.dto.ErrorDetailDTO;

import lombok.Data;

@Data
public class RestResponse<T> {
	@Expose
	private T state;

	@Expose
	private boolean mSuccess = true;

	@Expose
	private List<ErrorDetailDTO> mErrors = new ArrayList<>();

}
