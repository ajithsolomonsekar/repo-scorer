package com.ajith.reposcorer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "The response body containing the search results and scored repositories.")
public class RepositoryScoringResponse
{

    @Schema(description = "Metadata about the search query that was performed.")
    private SearchMetadata searchMetadata;

    @Schema(description = "A list of repositories that match the search criteria, sorted by score in descending order.")
    private List<ScoredRepository> repositories;

    @Schema(description = "The total number of repositories available on GitHub that match the search criteria.", example = "1500")
    private Integer totalCount;

    @Schema(description = "Indicates if the search results from GitHub were incomplete due to a timeout.", example = "false")
    private Boolean incompleteResults;

    @Schema(description = "The number of repositories returned in this response.", example = "30")
    private Integer returnedCount;

    @Schema(description = "The timestamp when the scoring was performed.")
    private LocalDateTime scoredAt;
}
