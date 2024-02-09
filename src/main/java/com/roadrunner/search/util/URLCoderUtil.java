package com.roadrunner.search.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.roadrunner.search.constants.BloomreachConstants;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class URLCoderUtil {

	/**
	 * @param text
	 * @return decoded text
	 */
	public static String decode(String text) {
		if (StringUtils.isEmpty(text)) {
			return text;
		}
		try {
			return URLDecoder.decode(text, BloomreachConstants.UTF_8);
		} catch (UnsupportedEncodingException exception) {
			log.error("URLCoderUtil::decode::UnsupportedEncodingException ={}", exception);
			return text;
		}
	}

	/**
	 * @param text
	 * @return encoded text
	 */
	public static String encode(String text) {
		if (StringUtils.isEmpty(text)) {
			return text;
		}
		try {
			return URLEncoder.encode(text, BloomreachConstants.UTF_8);
		} catch (UnsupportedEncodingException exception) {
			log.error("URLCoderUtil::encode::UnsupportedEncodingException ={}", exception);
			return text;
		}
	}
}
