package com.roadrunner.search.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix = "bloomreachconfig")
public class BloomreachConfiguration {
	private String searchApiUrl;
   
	private String accountId;

    private String authKey;

    private String domainKey;

    private List<String> fl;

    private String requestType;

    private String searchType;

    private String catagorytype;
    

}
