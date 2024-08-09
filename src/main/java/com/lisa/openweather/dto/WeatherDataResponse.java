package com.lisa.openweather.dto;

import com.lisa.openweather.response.WeatherInfo;
import lombok.Data;

import java.util.List;

@Data
public class WeatherDataResponse {
    List<WeatherInfo> weather;
}
