package com.lisa.openweather.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// JSON data format of weather from OpenWeatherMap
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherInfo {
    private Long id;
    private String main;
    private String description;
    private String icon;
}
