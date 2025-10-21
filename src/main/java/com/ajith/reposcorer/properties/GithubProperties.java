package com.ajith.reposcorer.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "github")
@Data
public class GithubProperties
{
    private String apiUrl = "https://api.github.com";
    private String token;
    private Integer connectTimeout = 5000;
    private Integer readTimeout = 10000;
}
