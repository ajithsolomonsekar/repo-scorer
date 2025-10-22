package com.ajith.reposcorer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A GitHub repository with its calculated popularity score.")
public class ScoredRepository
{

    @Schema(description = "The name of the repository.", example = "spring-boot")
    private String name;

    @Schema(description = "The full name of the repository, including the owner.", example = "spring-projects/spring-boot")
    private String fullName;

    @Schema(description = "The description of the repository.", example = "Spring Boot")
    private String description;

    @Schema(description = "The URL to the repository's GitHub page.", example = "https://github.com/spring-projects/spring-boot")
    private String url;

    @Schema(description = "The number of stars the repository has.", example = "65000")
    private Integer stars;

    @Schema(description = "The number of forks the repository has.", example = "37000")
    private Integer forks;

    @Schema(description = "The timestamp of the last update to the repository.")
    private LocalDateTime updatedAt;

    @Schema(description = "The final calculated score for the repository (0-100).", example = "88.5")
    private Double score;

    @Schema(description = "A breakdown of the individual components of the score.")
    private ScoreBreakdown scoreBreakdown;
}
