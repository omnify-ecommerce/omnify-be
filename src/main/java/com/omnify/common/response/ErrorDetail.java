package com.omnify.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(
    String field,
    String message
) {
    
    public static ErrorDetail of(String field, String message) {
        return new ErrorDetail(field, message);
    }

    public static ErrorDetail global(String message) {
        return new ErrorDetail(null, message);
    }
}
