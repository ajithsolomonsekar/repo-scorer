package com.ajith.reposcorer.mapper;

import com.ajith.reposcorer.client.dto.GithubRepository;
import com.ajith.reposcorer.dto.RepositoryScoringResponse;
import com.ajith.reposcorer.dto.RepositorySearchRequest;
import com.ajith.reposcorer.dto.ScoreBreakdown;
import com.ajith.reposcorer.dto.ScoreResult;
import com.ajith.reposcorer.dto.ScoredRepository;
import com.ajith.reposcorer.dto.SearchMetadata;
import java.time.LocalDateTime;
import java.util.List;
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
                ScoreBreakdown.builder()
                    .starsScore(scoreResult.getStarsScore())
                    .forksScore(scoreResult.getForksScore())
                    .recencyScore(scoreResult.getRecencyScore())
                    .build())
            .build();
    }


    public RepositoryScoringResponse toRepositoryScoringResponse(
        RepositorySearchRequest request,
        List<ScoredRepository> scoredRepositories,
        int totalCount,
        boolean incompleteResults
    )
    {
        return RepositoryScoringResponse.builder()
            .searchMetadata(SearchMetadata.builder()
                .language(request.getLanguage())
                .createdAfter(request.getCreatedAfter())
                .build())
            .repositories(scoredRepositories)
            .totalCount(totalCount)
            .incompleteResults(incompleteResults)
            .returnedCount(scoredRepositories.size())
            .scoredAt(LocalDateTime.now())
            .build();
    }
}
