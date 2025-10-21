package com.ajith.reposcorer.dto;

import java.time.LocalDate;
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
public class RepositoryScoringResponse
{
    private SearchMetadata searchMetadata;
    private List<ScoredRepository> repositories;
    private Integer totalCount;
    private Integer returnedCount;
    private LocalDateTime scoredAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchMetadata {
        private String language;
        private LocalDate createdAfter;
    }
}
