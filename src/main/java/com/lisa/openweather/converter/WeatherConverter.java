package com.lisa.openweather.converter;

import com.lisa.openweather.dto.WeatherDTO;

public class WeatherConverter {
    public static WeatherDTO convertWeather(String description) {
        return new WeatherDTO(description);
    }
}
