package com.roadrunner.search.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;
import com.roadrunner.search.config.BloomreachConfiguration;
import com.roadrunner.search.constants.BloomreachConstants;
import com.roadrunner.search.dto.BloomreachSearchResponseDTO;
import com.roadrunner.search.service.BloomreachSearchService;
import com.roadrunner.search.util.BloomreachSearchUtil;
import com.roadrunner.search.util.URLCoderUtil;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class BloomreachSearchServiceImpl implements BloomreachSearchService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Gson gson;

	@Autowired
	private BloomreachConfiguration bloomreachConfiguration;

	@Autowired
	private BloomreachSearchUtil bloomreachSearchUtil;

	@Override
	public String bloomreachSearchApiCall(String reqURL) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		log.debug("BloomreachServiceImpl::bloomreachServiceCall:: START...{} BRReqURL{}", stopWatch.getTime(), reqURL);
		String responseJson = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<String>(null, headers);
			String searchUrl = bloomreachConfiguration.getSearchApiUrl().replaceAll(BloomreachConstants.REGEX,
					BloomreachConstants.EMPTY_STRING);
			String[] split = reqURL.replace(searchUrl, BloomreachConstants.EMPTY_STRING)
					.split(BloomreachConstants.URL_DELIMETER);
			MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
			Arrays.stream(split).filter(StringUtils::isNotEmpty).map(params -> params.split(BloomreachConstants.EQUAL))
					.filter(parameter -> ArrayUtils.isNotEmpty(parameter) && parameter.length > 1)
					.forEach(parameter -> paramMap.add(parameter[0], URLCoderUtil.decode(parameter[1])));
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(searchUrl).queryParams(paramMap);
			URI uri = new URI(
					builder.toUriString().replaceAll(BloomreachConstants.PERCENTAGE_2B, BloomreachConstants.PLUS));
			log.debug("BloomreachServiceImpl::bloomreachServiceCall uri {}, builder {}", uri, builder);
			try {
				ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
				if (response != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
					responseJson = response.getBody();
				}
			} catch (HttpClientErrorException ex) {
				log.error(
						"BloomreachServiceImpl::bloomreachServiceCall HttpClientErrorException :: uri : {} exception={}",
						uri, ex);
			}
			log.debug("BloomreachServiceImpl::bloomreachServiceCall uri {}, responseJson {} ", uri, responseJson);
		} catch (Exception exception) {
			log.error("BloomreachServiceImpl::bloomreachServiceCall Exception reqUrl{} exception={}", reqURL,
					exception);
		}
		stopWatch.stop();
		log.debug("BloomreachServiceImpl::bloomreachServiceCall::END...{}", stopWatch.getTime());
		return responseJson;
	}

	@Override
	public BloomreachSearchResponseDTO populateBloomreachResponse(String productId) {
		BloomreachSearchResponseDTO bloomreachSearchResponseDTO = null;
		if (!StringUtils.isEmpty(productId)) {
			log.debug("BloomreachServiceImpl::populateBloomreachResponse..productId={}", productId);
			bloomreachSearchResponseDTO = new BloomreachSearchResponseDTO();
			Map<String, String> paramMap = new HashMap<>();
			paramMap.put(BloomreachConstants.Q, productId);
			Map<String, String> populateRequestParam = bloomreachSearchUtil.populateRequestParam(paramMap);
			String paramString = bloomreachSearchUtil.formBloomreachParamUrl(populateRequestParam);
			String url = MessageFormat.format(bloomreachConfiguration.getSearchApiUrl(), paramString);
			log.debug("BloomreachServiceImpl::populateBloomreachResponse..url={}", url);
			String responseJson = null;
			responseJson = bloomreachSearchApiCall(url);
			if (null != responseJson) {
				bloomreachSearchResponseDTO = gson.fromJson(responseJson.toString(), BloomreachSearchResponseDTO.class);
			}
			log.debug("BloomreachServiceImpl::populateBloomreachResponse..END");
		}
		return bloomreachSearchResponseDTO;
	}

	@Override
	public String bloomreachApiCall(String url) {
		log.debug("BloomreachServiceImpl::bloomreachApiCall START url{}", url);
		URI urls = null;
		try {
			urls = new URI(url);
		} catch (URISyntaxException uriSyntaxException) {
			log.error("BloomreachServiceImpl::bloomreachApiCall Exception url{} exception={}", url, uriSyntaxException);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		String responseJson = null;
		try {
			ResponseEntity<String> response = restTemplate.exchange(urls, HttpMethod.GET, entity, String.class);
			if (response != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
				responseJson = response.getBody();
			}
		} catch (HttpClientErrorException ex) {
			log.error("BloomreachServiceImpl::bloomreachApiCall HttpClientErrorException :: url : {} exception={}", url,
					ex);
		}
		log.debug("BloomreachServiceImpl::bloomreachApiCall END responseJson{}", responseJson);
		return responseJson;
	}

}
