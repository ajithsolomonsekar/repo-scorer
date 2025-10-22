package com.ajith.reposcorer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A breakdown of the individual components that make up the total score.")
public class ScoreBreakdown
{
    @Schema(description = "The portion of the score derived from stars.", example = "45.0")
    private Double starsScore;

    @Schema(description = "The portion of the score derived from forks.", example = "25.5")
    private Double forksScore;

    @Schema(description = "The portion of the score derived from its recent activity.", example = "18.0")
    private Double recencyScore;
}
