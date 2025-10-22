package com.ajith.reposcorer.service;

import com.ajith.reposcorer.client.dto.GithubRepository;
import com.ajith.reposcorer.dto.ScoreResult;
import com.ajith.reposcorer.dto.ScoredRepository;
import com.ajith.reposcorer.mapper.RepositoryMapper;
import com.ajith.reposcorer.properties.ScoringProperties;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryScoringService
{
    private final ScoringProperties scoringProperties;
    private final RepositoryMapper repositoryMapper;


    public List<ScoredRepository> scoreAndMapRepository(List<GithubRepository> repositories)
    {
        log.debug("Starting scoring process for {} repositories.", repositories.size());

        IntSummaryStatistics starsStats = repositories.stream().mapToInt(GithubRepository::getStargazersCount).summaryStatistics();
        IntSummaryStatistics forksStats = repositories.stream().mapToInt(GithubRepository::getForksCount).summaryStatistics();
        int minStars = starsStats.getMin();
        int maxStars = Math.max(starsStats.getMax(), 1);
        int minForks = forksStats.getMin();
        int maxForks = Math.max(forksStats.getMax(), 1);

        log.debug("Normalization boundary: stars [{}, {}], forks [{}, {}]", minStars, maxStars, minForks, maxForks);

        Map<GithubRepository, ScoreResult> repositoryScoreResultMap = repositories.stream()
            .collect(Collectors.toMap(
                repo -> repo,
                repo -> calculateScoreWithMinMax(repo, minStars, maxStars, minForks, maxForks)
            ));

        return repositoryScoreResultMap.entrySet().stream()
            .map(entry -> repositoryMapper.toScoredRepository(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(ScoredRepository::getScore).reversed())
            .toList();
    }


    private ScoreResult calculateScoreWithMinMax(GithubRepository githubRepository, int minStars, int maxStars, int minForks, int maxForks)
    {
        double starsScore = minMaxNormalize(githubRepository.getStargazersCount(), minStars, maxStars) * 100;
        double forksScore = minMaxNormalize(githubRepository.getForksCount(), minForks, maxForks) * 100;
        double recencyScore = calculateRecencyScore(githubRepository.getUpdatedAt()) * 100;

        double totalScore = (starsScore * scoringProperties.getStarsWeight())
            + (forksScore * scoringProperties.getForksWeight())
            + (recencyScore * scoringProperties.getRecencyWeight());

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


    private double minMaxNormalize(int value, int min, int max)
    {
        if (max == min)
        {
            return 1.0;
        }
        return (double) (value - min) / (max - min);
    }


    // Calculate recency score using exponential decay.
    private double calculateRecencyScore(LocalDateTime updatedAt)
    {
        if (updatedAt == null)
        {
            return 0.0;
        }

        long daysSinceUpdate = ChronoUnit.DAYS.between(updatedAt, LocalDateTime.now());
        double halfLife = scoringProperties.getRecencyHalfLifeDays();

        return Math.exp(-daysSinceUpdate / halfLife);
    }


    private double round(double value)
    {
        return Math.round(value * 100.0) / 100.0;
    }
}
