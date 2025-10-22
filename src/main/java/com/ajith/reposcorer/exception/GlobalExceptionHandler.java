package com.ajith.reposcorer.exception;

import com.ajith.reposcorer.dto.ErrorResponse;
import com.ajith.reposcorer.dto.ValidationErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
{

    @ExceptionHandler(GithubApiException.class)
    public ResponseEntity<ErrorResponse> handleGithubApiException(GithubApiException ex)
    {
        log.error("GitHub API error: {}", ex.getMessage(), ex);

        HttpStatus status = ex.getStatusCode() != null && ex.getStatusCode() == 403
            ? HttpStatus.SERVICE_UNAVAILABLE
            : HttpStatus.BAD_GATEWAY;

        ErrorResponse error = new ErrorResponse(
            status.value(),
            "GitHub API Error",
            ex.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(error);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex)
    {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> Objects.toString(fieldError.getDefaultMessage(), "No message available"),
                (existingValue, newValue) -> existingValue
            ));

        ValidationErrorResponse response = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            errors,
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex)
    {
        log.warn("Failed to read request body: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        String message = "Invalid JSON format";

        if (ex.getCause() instanceof InvalidFormatException ifx && ifx.getTargetType().equals(LocalDate.class))
        {
            String fieldName = ifx.getPath().getLast().getFieldName();
            errors.put(fieldName, "Invalid date format. Please use 'yyyy-MM-dd'.");
            message = "Validation Failed";
        }
        else
        {
            errors.put("requestBody", "The request body is malformed or contains invalid JSON.");
        }

        ValidationErrorResponse response = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            message,
            errors,
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex)
    {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
