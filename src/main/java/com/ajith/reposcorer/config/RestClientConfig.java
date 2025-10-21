package com.ajith.reposcorer.config;

import com.ajith.reposcorer.properties.GithubProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig
{
    private final GithubProperties githubProperties;


    @Bean
    public RestClient githubRestClient(RestClient.Builder builder)
    {
        var clientBuilder = builder
            .baseUrl(githubProperties.getApiUrl())
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "Repo-Scorer");

        if (githubProperties.getToken() != null && !githubProperties.getToken().isBlank())
        {
            clientBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubProperties.getToken());
        }

        return clientBuilder.build();
    }
}
