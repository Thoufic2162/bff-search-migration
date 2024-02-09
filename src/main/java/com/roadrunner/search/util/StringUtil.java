package com.roadrunner.search.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.constants.SearchConstants;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class StringUtil {

	/**
	 * @param url
	 * @param gender
	 * @param brand
	 * @param displayName
	 * @return
	 */
	public static String getSeoUrl(String url, String gender, String brand, String displayName) {
		String seoUrl = url;
		if (displayName != null) {
			displayName = displayName.toLowerCase();
			String[] tokens = displayName.split(SearchConstants.WHITESPACE_REGEX);
			int cnt;
			for (cnt = 0; cnt < tokens.length - 1; cnt++) {
				tokens[cnt] = tokens[cnt].replaceAll(SearchConstants.NON_WORD_REGEX, SearchConstants.EMPTY_STRING);
				if (tokens[cnt].length() > 0) {
					seoUrl = seoUrl.concat(tokens[cnt]).concat(SearchConstants.HYPHEN_STRING);
				}
			}
			tokens[cnt] = tokens[cnt].replaceAll(SearchConstants.NON_WORD_REGEX, SearchConstants.EMPTY_STRING);
			seoUrl = seoUrl.concat(tokens[cnt]);
		}
		return seoUrl;
	}

	public static String getEncodedValue(String value) {
		String res = value;
		try {
			res = URLEncoder.encode(value, BloomreachConstants.UTF_8);
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			log.error("StringUtil :: getEncodedValue() unsupportedEncodingException value={}", value);
		}
		return res;
	}

	public static double getDoubleFromString(String doubleStr) {
		double res = 0;
		try {
			if (StringUtils.isNotEmpty(doubleStr)) {
				res = Double.parseDouble(doubleStr);
			}
		} catch (NumberFormatException numberFormatException) {
			log.error("StringUtil :: getDoubleFromString() numberFormatException");
		}
		return res;
	}

	public static String getStringFromObject(Object value) {
		String res = SearchConstants.EMPTY_STRING;
		if (value == null) {
			return res;
		}
		res = value.toString();
		return res;
	}

	public static String listToString(List<String> values) {
		return listToString(values, SearchConstants.COMMA);
	}

	public static String listToString(List<String> values, String delimeter) {
		if (CollectionUtils.isEmpty(values)) {
			return SearchConstants.EMPTY_STRING;
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < values.size(); i++) {
			buffer.append(values.get(i));
			if (i + 1 < values.size()) {
				buffer.append(delimeter);
			}
		}
		return buffer.toString();
	}
}
