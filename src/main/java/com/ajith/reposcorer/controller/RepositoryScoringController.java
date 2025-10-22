package com.ajith.reposcorer.controller;

import com.ajith.reposcorer.dto.RepositoryScoringResponse;
import com.ajith.reposcorer.dto.RepositorySearchRequest;
import com.ajith.reposcorer.service.RepositorySearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Repository Scoring", description = "Endpoints for searching and scoring GitHub repositories")
public class RepositoryScoringController
{

    private final RepositorySearchService repositorySearchService;


    @Operation(summary = "Search and score repositories",
        description = "Searches public GitHub repositories based on language and creation date, then scores them based on popularity and activity.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved and scored repositories",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = RepositoryScoringResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters",
            content = @Content),
        @ApiResponse(responseCode = "502", description = "Error communicating with the GitHub API",
            content = @Content)
    })
    @PostMapping("/search")
    public ResponseEntity<RepositoryScoringResponse> searchRepositories(
        @Valid @RequestBody RepositorySearchRequest request)
    {
        log.info("Received search request: {}", request);
        RepositoryScoringResponse response = repositorySearchService.searchAndScoreRepositories(request);
        return ResponseEntity.ok(response);
    }
}
