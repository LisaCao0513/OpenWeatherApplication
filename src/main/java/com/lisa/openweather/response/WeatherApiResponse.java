package com.lisa.openweather.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherApiResponse<T> {
    private int status;
    private T data;
    private boolean success;
    private String errorMsg;

    public static <K> WeatherApiResponse<K> success(int status, K data) {
        return new WeatherApiResponse<>(status, data, true, null);
    }

    public static <K> WeatherApiResponse<K> fail(int status, String errorMsg) {
        return new WeatherApiResponse<>(status, null, false, errorMsg);
    }
}
