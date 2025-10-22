package com.ajith.reposcorer.mapper;

import com.ajith.reposcorer.client.dto.GithubRepository;
import com.ajith.reposcorer.dto.RepositoryScoringResponse;
import com.ajith.reposcorer.dto.RepositorySearchRequest;
import com.ajith.reposcorer.dto.ScoreResult;
import com.ajith.reposcorer.dto.ScoredRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryMapperTest
{

    private RepositoryMapper repositoryMapper;


    @BeforeEach
    void setUp()
    {
        repositoryMapper = new RepositoryMapper();
    }


    @Test
    void toScoredRepository_mapsAllFieldsCorrectly()
    {
        // Given
        GithubRepository githubRepo = GithubRepository.builder()
            .name("test-repo")
            .fullName("org/test-repo")
            .description("A test repository")
            .htmlUrl("http://github.com/org/test-repo")
            .stargazersCount(150)
            .forksCount(25)
            .updatedAt(LocalDateTime.now())
            .build();

        ScoreResult scoreResult = ScoreResult.builder()
            .totalScore(85.5)
            .starsScore(90.0)
            .forksScore(75.0)
            .recencyScore(95.0)
            .build();

        // When
        ScoredRepository scoredRepository = repositoryMapper.toScoredRepository(githubRepo, scoreResult);

        // Then
        assertThat(scoredRepository).isNotNull();
        assertThat(scoredRepository.getName()).isEqualTo(githubRepo.getName());
        assertThat(scoredRepository.getFullName()).isEqualTo(githubRepo.getFullName());
        assertThat(scoredRepository.getDescription()).isEqualTo(githubRepo.getDescription());
        assertThat(scoredRepository.getUrl()).isEqualTo(githubRepo.getHtmlUrl());
        assertThat(scoredRepository.getStars()).isEqualTo(githubRepo.getStargazersCount());
        assertThat(scoredRepository.getForks()).isEqualTo(githubRepo.getForksCount());
        assertThat(scoredRepository.getUpdatedAt()).isEqualTo(githubRepo.getUpdatedAt());
        assertThat(scoredRepository.getScore()).isEqualTo(scoreResult.getTotalScore());
        assertThat(scoredRepository.getScoreBreakdown()).isNotNull();
        assertThat(scoredRepository.getScoreBreakdown().getStarsScore()).isEqualTo(scoreResult.getStarsScore());
        assertThat(scoredRepository.getScoreBreakdown().getForksScore()).isEqualTo(scoreResult.getForksScore());
        assertThat(scoredRepository.getScoreBreakdown().getRecencyScore()).isEqualTo(scoreResult.getRecencyScore());
    }


    @Test
    void toRepositoryScoringResponse_mapsAllFieldsCorrectly()
    {
        // Given
        RepositorySearchRequest request = RepositorySearchRequest.builder()
            .language("java")
            .createdAfter(LocalDate.now().minusDays(10))
            .build();

        List<ScoredRepository> scoredRepositories = List.of(
            ScoredRepository.builder().name("repo1").build(),
            ScoredRepository.builder().name("repo2").build()
        );

        int totalCount = 500;
        boolean incompleteResults = false;

        // When
        RepositoryScoringResponse response = repositoryMapper.toRepositoryScoringResponse(
            request, scoredRepositories, totalCount, incompleteResults);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalCount()).isEqualTo(totalCount);
        assertThat(response.getIncompleteResults()).isEqualTo(incompleteResults);
        assertThat(response.getReturnedCount()).isEqualTo(scoredRepositories.size());
        assertThat(response.getRepositories()).isEqualTo(scoredRepositories);
        assertThat(response.getScoredAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(response.getSearchMetadata()).isNotNull();
        assertThat(response.getSearchMetadata().getLanguage()).isEqualTo(request.getLanguage());
        assertThat(response.getSearchMetadata().getCreatedAfter()).isEqualTo(request.getCreatedAfter());
    }
}
