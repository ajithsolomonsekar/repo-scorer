package com.ajith.reposcorer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Details of the search query.")
public class SearchMetadata
{
    @Schema(description = "The programming language that was searched for.", example = "java")
    private String language;

    @Schema(description = "The minimum creation date that was used for the search.", example = "2020-01-01")
    private LocalDate createdAfter;
}
