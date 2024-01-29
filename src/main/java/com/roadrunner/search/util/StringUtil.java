package com.roadrunner.search.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;

import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.constants.SearchConstants;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class StringUtil {
	public static String getSeoUrl(String url, String gender, String brand, String displayName) {
		String seoUrl = url;
		if (displayName != null) {
			displayName = displayName.toLowerCase();
			String[] tokens = displayName.split("\\s+");
			int cnt;
			for (cnt = 0; cnt < tokens.length - 1; cnt++) {
				tokens[cnt] = tokens[cnt].replaceAll("[\\W]|_", SearchConstants.EMPTY_STRING);
				if (tokens[cnt].length() > 0) {
					seoUrl = seoUrl.concat(tokens[cnt]).concat(SearchConstants.HYPHEN_STRING);
				}
			}
			tokens[cnt] = tokens[cnt].replaceAll("[\\W]|_", SearchConstants.EMPTY_STRING);
			seoUrl = seoUrl.concat(tokens[cnt]);
		}
		return seoUrl;
	}

	public static String getEncodedValue(String value) {
		String res = value;
		try {
			res = URLEncoder.encode(value, BloomreachConstants.UTF_8);
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			log.error("StringUtil :: getEncodedValue() unsupportedEncodingException", unsupportedEncodingException);
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
			log.error("StringUtil :: getDoubleFromString() numberFormatException", numberFormatException);
		}
		return res;
	}

}
