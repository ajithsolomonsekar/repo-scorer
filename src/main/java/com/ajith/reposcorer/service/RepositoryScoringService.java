package com.ajith.reposcorer.service;

import com.ajith.reposcorer.client.dto.GithubRepository;
import com.ajith.reposcorer.dto.ScoreResult;
import com.ajith.reposcorer.dto.ScoredRepository;
import com.ajith.reposcorer.mapper.RepositoryMapper;
import com.ajith.reposcorer.properties.ScoringProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryScoringService {

    private record NormalisationStats(int minStars, int maxStars, int minForks, int maxForks) {}

    private final ScoringProperties properties;
    private final RepositoryMapper repositoryMapper;

    public List<ScoredRepository> scoreAndMapRepository(List<GithubRepository> repositories) {
        if (repositories == null || repositories.isEmpty()) {
            log.debug("No repositories provided for scoring. Returning empty list.");
            return List.of();
        }

        log.debug("Starting scoring process for {} repositories.", repositories.size());

        NormalisationStats stats = calculateBatchStatistics(repositories);

        Map<GithubRepository, ScoreResult> repositoryScoreResultMap = repositories.stream()
            .collect(Collectors.toMap(
                repo -> repo,
                repo -> calculateScoreWithMinMax(repo, stats)
            ));

        List<ScoredRepository> scoredRepositories = repositoryScoreResultMap.entrySet().stream()
            .map(entry -> repositoryMapper.toScoredRepository(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(ScoredRepository::getScore).reversed())
            .toList();

        log.debug("Finished scoring and mapping {} repositories.", scoredRepositories.size());
        return scoredRepositories;
    }

    private NormalisationStats calculateBatchStatistics(List<GithubRepository> repositories) {
        IntSummaryStatistics starsStats = repositories.stream().mapToInt(GithubRepository::getStargazersCount).summaryStatistics();
        IntSummaryStatistics forksStats = repositories.stream().mapToInt(GithubRepository::getForksCount).summaryStatistics();

        int minStars = starsStats.getMin();
        int maxStars = Math.max(starsStats.getMax(), 1);
        int minForks = forksStats.getMin();
        int maxForks = Math.max(forksStats.getMax(), 1);

        log.debug("Normalization bounds: stars [{}, {}], forks [{}, {}]", minStars, maxStars, minForks, maxForks);
        return new NormalisationStats(minStars, maxStars, minForks, maxForks);
    }

    private ScoreResult calculateScoreWithMinMax(GithubRepository githubRepository, NormalisationStats stats) {
        double starsScore = minMaxNormalize(githubRepository.getStargazersCount(), stats.minStars(), stats.maxStars()) * 100;
        double forksScore = minMaxNormalize(githubRepository.getForksCount(), stats.minForks(), stats.maxForks()) * 100;
        double recencyScore = calculateRecencyScore(githubRepository.getUpdatedAt()) * 100;

        double totalScore = (starsScore * properties.getStarsWeight())
            + (forksScore * properties.getForksWeight())
            + (recencyScore * properties.getRecencyWeight());

        log.debug(
            "Scored {}: stars={}, forks={}, recency={}, total={}",
            githubRepository.getName(), round(starsScore), round(forksScore), round(recencyScore), round(totalScore));

        return ScoreResult.builder()
            .starsScore(round(starsScore))
            .forksScore(round(forksScore))
            .recencyScore(round(recencyScore))
            .totalScore(round(totalScore))
            .build();
    }

    private double minMaxNormalize(int value, int min, int max) {
        if (max == min) {
            return 1.0; // If all values are the same, consider them all "max"
        }
        return (double) (value - min) / (max - min);
    }

    private double calculateRecencyScore(LocalDateTime updatedAt) {
        if (updatedAt == null) {
            return 0.0;
        }

        long daysSinceUpdate = ChronoUnit.DAYS.between(updatedAt, LocalDateTime.now());
        double halfLife = properties.getRecencyHalfLifeDays();

        return Math.exp(-daysSinceUpdate / halfLife);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
