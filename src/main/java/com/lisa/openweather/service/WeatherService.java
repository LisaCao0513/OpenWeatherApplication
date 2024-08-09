package com.lisa.openweather.service;

import com.lisa.openweather.dao.Weather;
import com.lisa.openweather.dao.WeatherRepository;
import com.lisa.openweather.dto.WeatherDataResponse;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class WeatherService{

    private final WeatherRepository weatherRepository;

    private final RestTemplate restTemplate;

    @Value("${open.weather.apikey}")
    private String openWeatherApiKey;

    @Value("${open.weather.url}")
    private String openWeatherUrl;

    public WeatherService(WeatherRepository weatherRepository, RestTemplate restTemplate) {
        this.weatherRepository = weatherRepository;
        this.restTemplate = restTemplate;
    }

    public String getWeatherData(String city, String country) {
        String lowerCity = normalizeInput(city);
        String lowerCountry = normalizeInput(country);
        // Attempt to find the most recent weather description from the database
        Optional<Weather> existingWeather = weatherRepository.findTopByCityAndCountryOrderByIdDesc(lowerCity, lowerCountry);

        if (existingWeather.isPresent()) {
            return existingWeather.get().getDescription();
        } else {
            // Construct the API URL with query parameters
            String url = buildWeatherApiUrl(lowerCity, lowerCountry);
            // Fetch data from OpenWeatherMap
            return getWeatherFromApi(lowerCity, lowerCountry, url);
        }
    }

    private String getWeatherFromApi(String city, String country, String url) {
        try {
            ResponseEntity<WeatherDataResponse> responseEntity = restTemplate.getForEntity(url, WeatherDataResponse.class);
            WeatherDataResponse response = responseEntity.getBody();

            if (response != null && response.getWeather() != null && !response.getWeather().isEmpty()) {
                String description = response.getWeather().get(0).getDescription();
                saveWeatherToDb(city, country, description);
                return description;
            }
        } catch (HttpClientErrorException ex) {
            // Handle specific HTTP errors from Open Weather
            throw new HttpClientErrorException(ex.getStatusCode(),
                    "API request failed with message: " + ex.getMessage(), null, null, null);
        } catch (Exception ex) {
            // Handle other errors
            throw new RuntimeException(ex.getMessage());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Weather data not available");
    }

    private void saveWeatherToDb(String city, String country, String description) {
        // Store the new data in the database
        Weather weather = new Weather();
        weather.setCity(normalizeInput(city));
        weather.setCountry(normalizeInput(country));
        weather.setDescription(description);
        weatherRepository.save(weather);
    }

    private String buildWeatherApiUrl(@Nonnull String city, @Nonnull String country) {
        // Validate the URL and API key
        if (!StringUtils.hasText(openWeatherUrl)) {
            throw new IllegalArgumentException("Base URL must not be empty");
        }
        if (!StringUtils.hasText(openWeatherApiKey)) {
            throw new IllegalArgumentException("API Key must not be empty");
        }
        return UriComponentsBuilder.fromHttpUrl(openWeatherUrl)
                .queryParam("q", city + "," + country)
                .queryParam("appid", openWeatherApiKey)
                .toUriString();
    }

    private String normalizeInput(String input) {
        if (!StringUtils.hasText(input)) {
            throw new IllegalArgumentException("Input cannot be blank or null");
        }
        return input.trim().toLowerCase();
    }
}

