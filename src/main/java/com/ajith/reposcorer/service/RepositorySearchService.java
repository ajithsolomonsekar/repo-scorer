package com.ajith.reposcorer.service;

import com.ajith.reposcorer.client.GithubApiClient;
import com.ajith.reposcorer.client.dto.GithubRepository;
import com.ajith.reposcorer.dto.RepositoryScoringResponse;
import com.ajith.reposcorer.dto.RepositorySearchRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositorySearchService
{
    private final GithubApiClient githubApiClient;


    public RepositoryScoringResponse searchAndScoreRepositories(RepositorySearchRequest request)
    {
        log.debug("Searching repositories with criteria: {}", request);

        List<GithubRepository> repositories = githubApiClient.searchRepositories(
            request.getLanguage(),
            request.getCreatedAfter(),
            request.getMaxResults()
        );

        log.info("Found {} repositories from GitHub", repositories.size());

        return RepositoryScoringResponse.builder().build();
    }
}
