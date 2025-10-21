package com.ajith.reposcorer.controller;

import com.ajith.reposcorer.dto.RepositoryScoringResponse;
import com.ajith.reposcorer.dto.RepositorySearchRequest;
import com.ajith.reposcorer.service.RepositorySearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/repositories")
@RequiredArgsConstructor
@Slf4j
public class RepositoryScoringController
{

    private final RepositorySearchService repositorySearchService;


    @PostMapping("/search")
    public ResponseEntity<RepositoryScoringResponse> searchRepositories(
        @Valid @RequestBody RepositorySearchRequest request)
    {

        log.info(
            "Received search request: language={}, createdAfter={}",
            request.getLanguage(), request.getCreatedAfter());

        RepositoryScoringResponse response = repositorySearchService.searchAndScoreRepositories(request);

        log.info("Returning {} scored repositories", response);
        return ResponseEntity.ok(response);
    }

}
