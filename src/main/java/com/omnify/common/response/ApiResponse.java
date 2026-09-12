package com.omnify.common.response;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    String errorCode,
    List<ErrorDetail> errors,
    Instant timestamp
) {
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message, String errorCode, List<ErrorDetail> errors) {
        return new ApiResponse<>(false, message, null, errorCode, errors, Instant.now());
    }
}
