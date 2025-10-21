package com.ajith.reposcorer.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoredRepository
{
    private String name;
    private String fullName;
    private String description;
    private String url;
    private Integer stars;
    private Integer forks;
    private LocalDateTime updatedAt;
    private Double score;
    private ScoreBreakdown scoreBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreBreakdown {
        private Double starsScore;
        private Double forksScore;
        private Double recencyScore;
    }
}
