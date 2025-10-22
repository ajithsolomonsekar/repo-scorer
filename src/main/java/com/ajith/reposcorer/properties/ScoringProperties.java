package com.ajith.reposcorer.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "scoring")
@Data
public class ScoringProperties
{
    private Double starsWeight = 0.5;
    private Double forksWeight = 0.2;
    private Double recencyWeight = 0.3;
    private Double recencyHalfLifeDays = 180.0;
}
