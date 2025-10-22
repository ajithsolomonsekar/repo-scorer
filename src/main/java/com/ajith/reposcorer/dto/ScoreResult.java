package com.ajith.reposcorer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoreResult
{
    private Double starsScore;
    private Double forksScore;
    private Double recencyScore;
    private Double totalScore;
}
