package com.ajith.reposcorer.mapper;

import com.ajith.reposcorer.client.dto.GithubRepository;
import com.ajith.reposcorer.dto.ScoreResult;
import com.ajith.reposcorer.dto.ScoredRepository;
import org.springframework.stereotype.Component;

@Component
public class RepositoryMapper
{

    public ScoredRepository toScoredRepository(GithubRepository githubRepository, ScoreResult scoreResult)
    {
        return ScoredRepository.builder()
            .name(githubRepository.getName())
            .fullName(githubRepository.getFullName())
            .description(githubRepository.getDescription())
            .url(githubRepository.getHtmlUrl())
            .stars(githubRepository.getStargazersCount())
            .forks(githubRepository.getForksCount())
            .updatedAt(githubRepository.getUpdatedAt())
            .score(scoreResult.getTotalScore())
            .scoreBreakdown(
                ScoredRepository.ScoreBreakdown.builder()
                    .starsScore(scoreResult.getStarsScore())
                    .forksScore(scoreResult.getForksScore())
                    .recencyScore(scoreResult.getRecencyScore())
                    .build())
            .build();
    }
}
