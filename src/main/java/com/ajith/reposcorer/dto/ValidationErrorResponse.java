package com.ajith.reposcorer.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationErrorResponse
{
    private Integer status;
    private String error;
    private Map<String, String> validationErrors;
    private LocalDateTime timestamp;
}
