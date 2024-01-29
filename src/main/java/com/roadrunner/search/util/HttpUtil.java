package com.roadrunner.search.util;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import com.roadrunner.search.constants.SearchConstants;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

@SuppressWarnings("rawtypes")
public class HttpUtil {

	public static Properties getRequestAttribute(ServletRequest request) {
		Properties requestParams = new Properties();
		Enumeration e = request.getAttributeNames();
		String name = null;
		Object value = null;
		while (e.hasMoreElements()) {
			name = (String) e.nextElement();
			value = null;
			String paramArr = request.getAttribute(name).toString();
			if (paramArr != null) {
				if (paramArr.length() > 1) {
					value = paramArr;
				} else {
					value = paramArr;
				}
				if (!name.equalsIgnoreCase(SearchConstants.QURI)) {
					requestParams.put(name, value);
				}
			}
		}
		return requestParams;
	}

	public static Properties getRequestParams(HttpServletRequest request) {
		Properties requestParams = new Properties();
		Enumeration e = request.getParameterNames();
		String name = null;
		Object value = null;
		while (e.hasMoreElements()) {
			name = (String) e.nextElement();
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
		return requestParams;
	}
}