package com.ajith.reposcorer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for searching repositories")
public class RepositorySearchRequest {

    @Schema(description = "The programming language to search for", example = "java", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Language is required")
    private String language;

    @Schema(description = "The minimum creation date for repositories (YYYY-MM-DD format)", example = "2020-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Created after date is required")
    private LocalDate createdAfter;

    @Schema(description = "The maximum number of results to return", example = "50", defaultValue = "30")
    @Builder.Default
    private Integer maxResults = 30;
}
