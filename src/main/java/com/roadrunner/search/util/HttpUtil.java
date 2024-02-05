package com.roadrunner.search.util;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import com.roadrunner.search.constants.SearchConstants;

import jakarta.servlet.http.HttpServletRequest;

public class HttpUtil {

	public static Properties getRequestAttributesAndParameters(HttpServletRequest request) {
		Properties requestParams = new Properties();
		Enumeration<String> attributes = request.getAttributeNames();
		Enumeration<String> params = request.getParameterNames();
		String name = null;
		Object value = null;
		while (params.hasMoreElements()) {
			name = (String) params.nextElement();
			value = null;
			String[] paramArr = request.getParameterValues(name);
			if (paramArr != null) {
				if (paramArr.length > 1) {
					value = Arrays.asList(paramArr);
				} else {
					value = paramArr[0];
				}
				if (!name.equalsIgnoreCase(SearchConstants.QURI)) {
					requestParams.put(name, value);
				}
			}
		}
		while (attributes.hasMoreElements()) {
			name = (String) attributes.nextElement();
			value = request.getAttribute(name).toString();
			if (value != null) {
				if (!name.equalsIgnoreCase(SearchConstants.QURI)) {
					requestParams.put(name, value);
				}
			}
		}
		return requestParams;
	}

}
