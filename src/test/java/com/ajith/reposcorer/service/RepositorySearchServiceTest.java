package com.ajith.reposcorer.service;

import com.ajith.reposcorer.client.GithubApiClient;
import com.ajith.reposcorer.client.dto.GithubRepository;
import com.ajith.reposcorer.client.dto.GithubSearchResponse;
import com.ajith.reposcorer.dto.RepositoryScoringResponse;
import com.ajith.reposcorer.dto.RepositorySearchRequest;
import com.ajith.reposcorer.dto.ScoredRepository;
import com.ajith.reposcorer.mapper.RepositoryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositorySearchServiceTest {

    @Mock
    private GithubApiClient githubApiClient;

    @Mock
    private RepositoryScoringService repositoryScoringService;

    @Mock
    private RepositoryMapper repositoryMapper;

    @InjectMocks
    private RepositorySearchService repositorySearchService;

    @Test
    void searchAndScoreRepositories_withValidRequest_returnsScoredResponse() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
            .language("java")
            .createdAfter(LocalDate.now().minusYears(1))
            .maxResults(10)
            .build();

        GithubRepository repoA = GithubRepository.builder().name("RepoA").build();
        GithubRepository repoB = GithubRepository.builder().name("RepoB").build();
        List<GithubRepository> githubRepositories = List.of(repoA, repoB);
        GithubSearchResponse githubSearchResponse = new GithubSearchResponse(100, false, githubRepositories);

        List<ScoredRepository> scoredRepositories = List.of(
            ScoredRepository.builder().name("RepoB").score(50.35).build(),
            ScoredRepository.builder().name("RepoA").score(46.93).build()
        );

        when(githubApiClient.searchRepositories(request.getLanguage(), request.getCreatedAfter(), request.getMaxResults()))
            .thenReturn(githubSearchResponse);

        when(repositoryScoringService.scoreAndMapRepository(githubRepositories))
            .thenReturn(scoredRepositories);

        RepositoryScoringResponse expectedResponse = RepositoryScoringResponse.builder().totalCount(100).build();
        when(repositoryMapper.toRepositoryScoringResponse(request, scoredRepositories, 100, false))
            .thenReturn(expectedResponse);

        // When
        RepositoryScoringResponse actualResponse = repositorySearchService.searchAndScoreRepositories(request);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getTotalCount()).isEqualTo(100);

        verify(githubApiClient, times(1)).searchRepositories(request.getLanguage(), request.getCreatedAfter(), request.getMaxResults());
        verify(repositoryScoringService, times(1)).scoreAndMapRepository(githubRepositories);
        verify(repositoryMapper, times(1)).toRepositoryScoringResponse(request, scoredRepositories, 100, false);
    }

    @Test
    void searchAndScoreRepositories_whenApiClientReturnsEmpty_returnsEmptyResponse() {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
            .language("unknown-lang")
            .createdAfter(LocalDate.now().minusYears(1))
            .build();

        GithubSearchResponse emptyGithubResponse = new GithubSearchResponse(0, false, List.of());
        List<ScoredRepository> emptyScoredList = List.of();

        // Mock the dependencies
        when(githubApiClient.searchRepositories(anyString(), any(LocalDate.class), anyInt()))
            .thenReturn(emptyGithubResponse);

        RepositoryScoringResponse expectedResponse = RepositoryScoringResponse.builder()
            .totalCount(0)
            .returnedCount(0)
            .repositories(List.of())
            .build();

        when(repositoryMapper.toRepositoryScoringResponse(request, emptyScoredList, 0, false))
            .thenReturn(expectedResponse);

        // When
        RepositoryScoringResponse actualResponse = repositorySearchService.searchAndScoreRepositories(request);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getTotalCount()).isZero();
        assertThat(actualResponse.getReturnedCount()).isZero();
        assertThat(actualResponse.getRepositories()).isEmpty();

        // Verify interactions
        verify(githubApiClient, times(1)).searchRepositories(anyString(), any(LocalDate.class), anyInt());
        verifyNoInteractions(repositoryScoringService);
        verify(repositoryMapper, times(1)).toRepositoryScoringResponse(request, emptyScoredList, 0, false);
    }
}
