package com.ajith.reposcorer.service;

import com.ajith.reposcorer.client.dto.GithubRepository;
import com.ajith.reposcorer.dto.ScoreResult;
import com.ajith.reposcorer.dto.ScoredRepository;
import com.ajith.reposcorer.mapper.RepositoryMapper;
import com.ajith.reposcorer.properties.ScoringProperties;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryScoringServiceTest
{

    @Mock(strictness = Mock.Strictness.LENIENT)
    private ScoringProperties scoringProperties;

    @Mock
    private RepositoryMapper repositoryMapper;

    @InjectMocks
    private RepositoryScoringService repositoryScoringService;


    @BeforeEach
    void setUp()
    {
        when(scoringProperties.getStarsWeight()).thenReturn(0.5);
        when(scoringProperties.getForksWeight()).thenReturn(0.3);
        when(scoringProperties.getRecencyWeight()).thenReturn(0.2);
        when(scoringProperties.getRecencyHalfLifeDays()).thenReturn(180.0);
    }


    @Test
    void scoreAndMapRepository_withEmptyList_returnsEmptyList()
    {
        // When
        List<ScoredRepository> result = repositoryScoringService.scoreAndMapRepository(List.of());

        // Then
        assertThat(result).isNotNull().isEmpty();
    }


    @Test
    void scoreAndMapRepository_withValidRepositories_calculatesScoresAndSortsCorrectly()
    {
        // Given
        GithubRepository repoA = GithubRepository.builder()
            .name("RepoA")
            .stargazersCount(100)
            .forksCount(200)
            .updatedAt(LocalDateTime.now().minusDays(30))
            .build();

        GithubRepository repoB = GithubRepository.builder()
            .name("RepoB")
            .stargazersCount(500) // Max stars
            .forksCount(50) // Min forks
            .updatedAt(LocalDateTime.now().minusDays(730))
            .build();

        List<GithubRepository> repositories = List.of(repoA, repoB);

        when(repositoryMapper.toScoredRepository(any(GithubRepository.class), any(ScoreResult.class)))
            .thenAnswer(invocation -> {
                GithubRepository repo = invocation.getArgument(0);
                ScoreResult score = invocation.getArgument(1);
                return ScoredRepository.builder()
                    .name(repo.getName())
                    .score(score.getTotalScore())
                    .build();
            });

        // When
        List<ScoredRepository> result = repositoryScoringService.scoreAndMapRepository(repositories);

        // Then
        // Repo B should have higher score than Repo A
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("RepoB");
        assertThat(result.get(0).getScore()).isEqualTo(50.35);
        assertThat(result.get(1).getName()).isEqualTo("RepoA");
        assertThat(result.get(1).getScore()).isEqualTo(46.93);

        assertThat(result).isSortedAccordingTo((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()));
    }
}
