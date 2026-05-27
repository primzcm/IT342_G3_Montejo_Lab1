package edu.cit.montejo.collabmatch.common.api;

import java.time.Instant;

public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final ApiError error;
    private final Instant timestamp;

    private ApiResponse(boolean success, T data, ApiError error) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        return new ApiResponse<>(false, null, new ApiError(code, message, details));
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public ApiError getError() {
        return error;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
