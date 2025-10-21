package com.ajith.reposcorer.client;

import com.ajith.reposcorer.client.dto.GithubRepository;
import com.ajith.reposcorer.client.dto.GithubSearchResponse;
import com.ajith.reposcorer.exception.GithubApiException;
import com.ajith.reposcorer.properties.GithubProperties;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class GithubApiClient
{
    private final RestClient githubRestClient;
    private final GithubProperties githubProperties;


    @Cacheable(value = "githubSearches", key = "#language + '-' + #createdAfter + '-' + #maxResults")
    public List<GithubRepository> searchRepositories(
        String language,
        LocalDate createdAfter,
        Integer maxResults)
    {

        String query = buildSearchQuery(language, createdAfter);
        String uri = buildSearchUri(query, maxResults);

        log.info("Calling GitHub API: {}", uri);

        try
        {
            GithubSearchResponse response = githubRestClient.get()
                .uri(uri)
                .retrieve()
                .body(GithubSearchResponse.class);

            return handleResponse(response);

        }
        catch (RestClientResponseException e)
        {
            throw handleRestClientException(e);
        }
        catch (Exception e)
        {
            log.error("Unexpected error calling GitHub API", e);
            throw new GithubApiException("Failed to search repositories: " + e.getMessage(), e);
        }
    }


    private List<GithubRepository> handleResponse(GithubSearchResponse response)
    {
        if (response == null || response.getItems() == null)
        {
            log.warn("GitHub API returned null response");
            return List.of();
        }

        log.info(
            "GitHub API returned {} repositories (total: {})",
            response.getItems().size(), response.getTotalCount());

        return response.getItems();
    }


    private GithubApiException handleRestClientException(RestClientResponseException e)
    {
        int statusCode = e.getStatusCode().value();
        String message = determineErrorMessage(statusCode, e);

        log.error("GitHub API error: {} - {}", statusCode, message);

        return new GithubApiException(message, statusCode);
    }


    private String determineErrorMessage(int statusCode, RestClientResponseException e)
    {
        return switch (statusCode)
        {
            case 401 -> "GitHub API authentication failed. Check your access token.";
            case 403 -> "GitHub API rate limit exceeded. Please try again later or add an access token.";
            case 404 -> "GitHub API endpoint not found.";
            case 422 -> "Invalid search query: " + e.getMessage();
            case 503 -> "GitHub API is currently unavailable. Please try again later.";
            default ->
            {
                if (statusCode >= 400 && statusCode < 500)
                {
                    yield "GitHub API client error (" + statusCode + "): " + e.getMessage();
                }
                else if (statusCode >= 500)
                {
                    yield "GitHub API server error (" + statusCode + "): " + e.getMessage();
                }
                yield "GitHub API error: " + e.getMessage();
            }
        };
    }


    private String buildSearchQuery(String language, LocalDate createdAfter)
    {
        return String.format("language:%s created:>%s", language.toLowerCase(), createdAfter.toString());
    }


    private String buildSearchUri(String query, Integer maxResults)
    {
        int perPage = Math.min(maxResults, 100); // GitHub API max per page is 100

        return UriComponentsBuilder
            .fromUriString(githubProperties.getApiUrl())
            .path("/search/repositories")
            .queryParam("q", query)
            .queryParam("sort", "stars")
            .queryParam("order", "desc")
            .queryParam("per_page", perPage)
            .build()
            .toUriString();
    }
}
