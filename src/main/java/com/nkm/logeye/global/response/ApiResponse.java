package com.nkm.logeye.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nkm.logeye.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final ErrorResponse error;

    private ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
       return new ApiResponse<>(false, null, new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String customMessage) {
        return new ApiResponse<>(false, null, new ErrorResponse(errorCode.getCode(), customMessage));
    }

    public static <T> ApiResponse<T> errorWithData(ErrorCode errorCode, T errorData) {
        return new ApiResponse<>(false, errorData, new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }

    private record ErrorResponse(String code, String message) {
    }
}