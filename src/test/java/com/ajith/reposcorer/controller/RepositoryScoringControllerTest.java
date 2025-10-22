package com.ajith.reposcorer.controller;

import com.ajith.reposcorer.dto.RepositoryScoringResponse;
import com.ajith.reposcorer.dto.RepositorySearchRequest;
import com.ajith.reposcorer.dto.ScoredRepository;
import com.ajith.reposcorer.service.RepositorySearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RepositoryScoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RepositorySearchService repositorySearchService;

    @Test
    void searchRepositories_withValidRequest_returns200Ok() throws Exception {
        // Given
        RepositorySearchRequest validRequest = RepositorySearchRequest.builder()
            .language("java")
            .createdAfter(LocalDate.now().minusYears(1))
            .build();

        RepositoryScoringResponse mockResponse = RepositoryScoringResponse.builder()
            .totalCount(1)
            .returnedCount(1)
            .repositories(List.of(ScoredRepository.builder().name("Test Repo").score(99.9).build()))
            .build();

        when(repositorySearchService.searchAndScoreRepositories(any(RepositorySearchRequest.class)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/repositories/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.repositories[0].name").value("Test Repo"));
    }

    @Test
    void searchRepositories_withInvalidRequest_returns400BadRequest() throws Exception {
        // Given
        RepositorySearchRequest invalidRequest = RepositorySearchRequest.builder()
            .language("")
            .createdAfter(null)
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/repositories/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.validationErrors.language").value("Language is required"))
            .andExpect(jsonPath("$.validationErrors.createdAfter").value("Created after date is required"));
    }
}
