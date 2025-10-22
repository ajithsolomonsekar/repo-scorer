package com.ajith.reposcorer.service;

import com.ajith.reposcorer.client.GithubApiClient;
import com.ajith.reposcorer.client.dto.GithubRepository;
import com.ajith.reposcorer.client.dto.GithubSearchResponse;
import com.ajith.reposcorer.dto.RepositoryScoringResponse;
import com.ajith.reposcorer.dto.RepositorySearchRequest;
import com.ajith.reposcorer.dto.ScoredRepository;
import com.ajith.reposcorer.mapper.RepositoryMapper;
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
    private final RepositoryScoringService repositoryScoringService;
    private final RepositoryMapper repositoryMapper;


    public RepositoryScoringResponse searchAndScoreRepositories(RepositorySearchRequest request)
    {
        log.debug("Searching repositories with criteria: {}", request);

        GithubSearchResponse searchResponse = githubApiClient.searchRepositories(
            request.getLanguage(),
            request.getCreatedAfter(),
            request.getMaxResults()
        );

        List<GithubRepository> repositories = searchResponse.getItems();
        int totalCount = searchResponse.getTotalCount();
        boolean incompleteResults = searchResponse.getIncompleteResults();

        log.info("Found {} repositories from GitHub (total matching: {}, incomplete: {})", repositories.size(), totalCount, incompleteResults);

        List<ScoredRepository> scoredRepositories = repositoryScoringService.scoreAndMapRepository(repositories);

        log.debug("Scored and sorted {} repositories", scoredRepositories.size());

        return repositoryMapper.toRepositoryScoringResponse(request, scoredRepositories, totalCount, incompleteResults);
    }

}
