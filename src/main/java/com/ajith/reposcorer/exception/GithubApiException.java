package com.ajith.reposcorer.exception;

import lombok.Getter;

@Getter
public class GithubApiException extends RuntimeException
{
    private final Integer statusCode;


    public GithubApiException(String message, Integer statusCode)
    {
        super(message);
        this.statusCode = statusCode;
    }


    public GithubApiException(String message, Throwable cause)
    {
        super(message, cause);
        this.statusCode = null;
    }
}
