package com.lisa.openweather.controller;

import com.lisa.openweather.converter.WeatherConverter;
import com.lisa.openweather.dto.WeatherDTO;
import com.lisa.openweather.response.WeatherApiResponse;
import com.lisa.openweather.service.ApiKeyService;
import com.lisa.openweather.service.WeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    private final ApiKeyService apiKeyService;

    public WeatherController(WeatherService weatherService, ApiKeyService apiKeyService) {
        this.weatherService = weatherService;
        this.apiKeyService = apiKeyService;
    }

    @GetMapping
    public WeatherApiResponse<WeatherDTO> getWeather(@RequestParam String city,
                                                     @RequestParam String country,
                                                     @RequestHeader(value = "X-Api-Key") String apiKey) {
        if (!apiKeyService.validateApiKey(apiKey)) {
            return WeatherApiResponse.fail(HttpStatus.FORBIDDEN.value(), "Invalid API key or limit exceeded. Max 5 api calls an hour.");
        }

        try {
            String description = weatherService.getWeatherData(city, country);
            return WeatherApiResponse.success(HttpStatus.OK.value(), WeatherConverter.convertWeather(description));
        } catch (Exception e) {
            return WeatherApiResponse.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public WeatherApiResponse<?> handleMissingParams(MissingServletRequestParameterException ex) {
        return WeatherApiResponse.fail(HttpStatus.BAD_REQUEST.value(), "Custom Error: '" + ex.getParameterName() + "' parameter is missing.");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public WeatherApiResponse<?> handleMissingHeader(MissingRequestHeaderException ex) {
        return WeatherApiResponse.fail(HttpStatus.BAD_REQUEST.value(), "Custom Error: '" + ex.getHeaderName() + "' header is missing.");
    }
}

