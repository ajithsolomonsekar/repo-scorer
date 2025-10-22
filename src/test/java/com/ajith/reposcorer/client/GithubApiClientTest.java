package com.ajith.reposcorer.client;

import com.ajith.reposcorer.client.dto.GithubRepository;
import com.ajith.reposcorer.client.dto.GithubSearchResponse;
import com.ajith.reposcorer.exception.GithubApiException;
import com.ajith.reposcorer.properties.GithubProperties;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubApiClientTest
{

    @Mock
    private RestClient githubRestClient;
    @Mock
    private GithubProperties githubProperties;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private GithubApiClient githubApiClient;


    @BeforeEach
    void setUp()
    {
        lenient().when(githubProperties.getApiUrl()).thenReturn("https://api.github.com");
        lenient().when(githubRestClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }


    @Test
    void searchRepositories_whenApiSucceeds_returnsSanitisedResponse()
    {
        // Given
        GithubSearchResponse mockResponse = new GithubSearchResponse(
            1, false, List.of(GithubRepository.builder().name("test-repo").build()));
        when(responseSpec.body(GithubSearchResponse.class)).thenReturn(mockResponse);

        // When
        GithubSearchResponse actualResponse = githubApiClient.searchRepositories("java", LocalDate.now(), 10);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getTotalCount()).isEqualTo(1);
        assertThat(actualResponse.getIncompleteResults()).isFalse();
        assertThat(actualResponse.getItems()).hasSize(1);
        assertThat(actualResponse.getItems().getFirst().getName()).isEqualTo("test-repo");
    }

    @Test
    void searchRepositories_whenApiReturnsNullItems_returnsSanitisedResponse()
    {
        // Given
        GithubSearchResponse mockResponse = new GithubSearchResponse(
            null, null, null);
        when(responseSpec.body(GithubSearchResponse.class)).thenReturn(mockResponse);

        // When
        GithubSearchResponse actualResponse = githubApiClient.searchRepositories("java", LocalDate.now(), 10);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getTotalCount()).isZero();
        assertThat(actualResponse.getIncompleteResults()).isFalse();
        assertThat(actualResponse.getItems()).isEmpty();
    }


    @Test
    void searchRepositories_whenApiReturnsNullBody_returnsEmptyIncompleteResponse()
    {
        // Given
        when(responseSpec.body(GithubSearchResponse.class)).thenReturn(null);

        // When
        GithubSearchResponse actualResponse = githubApiClient.searchRepositories("java", LocalDate.now(), 10);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getTotalCount()).isZero();
        assertThat(actualResponse.getIncompleteResults()).isTrue();
        assertThat(actualResponse.getItems()).isEmpty();
    }


    @Test
    void searchRepositories_whenApiReturns403_throwsGithubApiException()
    {
        // Given
        RestClientResponseException ex = new RestClientResponseException("Rate limit exceeded", HttpStatus.FORBIDDEN.value(), "Forbidden", null, null, null);
        when(responseSpec.body(GithubSearchResponse.class)).thenThrow(ex);

        // When & Then
        GithubApiException thrown = assertThrows(
            GithubApiException.class,
            () -> githubApiClient.searchRepositories("java", LocalDate.now(), 10));

        assertThat(thrown.getMessage()).contains("GitHub API rate limit exceeded");
        assertThat(thrown.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }


    @Test
    void searchRepositories_whenRestClientThrowsGenericException_throwsGithubApiException()
    {
        // Given
        RuntimeException ex = new RuntimeException("Network error");
        when(responseSpec.body(GithubSearchResponse.class)).thenThrow(ex);

        // When & Then
        GithubApiException thrown = assertThrows(
            GithubApiException.class,
            () -> githubApiClient.searchRepositories("java", LocalDate.now(), 10));

        assertThat(thrown.getMessage()).contains("Failed to search repositories: Network error");
    }
}
