package com.ajith.reposcorer.dto;

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
public class RepositorySearchRequest
{
    @NotBlank(message = "Language is required")
    private String language;

    @NotNull(message = "Created after date is required")
    private LocalDate createdAfter;

    @Builder.Default
    private Integer maxResults = 30;
}
