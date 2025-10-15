package com.wds.maytapi.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;    
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "maytapi")
public class MaytAPIConfig {
    private String baseUrl;
    private String productId;
    private String phoneId;
    private String apiToken;
}