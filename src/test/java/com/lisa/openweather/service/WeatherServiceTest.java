package com.lisa.openweather.service;

import com.lisa.openweather.dao.Weather;
import com.lisa.openweather.dao.WeatherRepository;
import com.lisa.openweather.dto.WeatherDataResponse;
import com.lisa.openweather.response.WeatherInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static com.lisa.openweather.Utils.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class WeatherServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(weatherService, "openWeatherUrl", OPEN_WEATHER_URL);
        ReflectionTestUtils.setField(weatherService, "openWeatherApiKey", OPEN_WEATHER_API_KEY);
    }

    @Test
    void whenDataExistsInDatabase_thenShouldReturnData() {
        // Given
        Optional<Weather> existingWeather = Optional.of(new Weather(1L, CITY, COUNTRY, DESCRIPTION_SUNNY));
        when(weatherRepository.findTopByCityAndCountryOrderByIdDesc(anyString(), anyString())).thenReturn(existingWeather);
        // When
        String description = weatherService.getWeatherData(CITY, COUNTRY);

        // Then
        assertEquals(DESCRIPTION_SUNNY, description);
        verify(weatherRepository).findTopByCityAndCountryOrderByIdDesc(CITY, COUNTRY);
        verifyNoInteractions(restTemplate); // Ensures no API call was made
    }

    @Test
    void whenDataDoesNotExistInDatabase_thenShouldFetchFromAPIAndSave() {
        // Given
        when(weatherRepository.findTopByCityAndCountryOrderByIdDesc(anyString(), anyString())).thenReturn(Optional.empty());
        WeatherInfo weatherInfo = new WeatherInfo();
        weatherInfo.setId(1L);
        weatherInfo.setDescription(DESCRIPTION_CLOUDY);
        WeatherDataResponse weatherDataResponse = new WeatherDataResponse();
        weatherDataResponse.setWeather(List.of(weatherInfo));

        ResponseEntity<WeatherDataResponse> responseEntity = new ResponseEntity<>(weatherDataResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(WeatherDataResponse.class)))
                .thenReturn(responseEntity);
        // When
        String description = weatherService.getWeatherData(CITY, COUNTRY);

        Weather savedWeather = new Weather(1L, CITY, COUNTRY, description);
        given(weatherRepository.save(any(Weather.class))).willReturn(savedWeather);

        // Then
        assertEquals(DESCRIPTION_CLOUDY, description);
        verify(weatherRepository).save(any(Weather.class));
        verify(weatherRepository, times(1)).findTopByCityAndCountryOrderByIdDesc(CITY, COUNTRY);
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(WeatherDataResponse.class));
    }

    @Test
    void testWebClientResponseException_FetchFromAPI() {
        when(weatherRepository.findTopByCityAndCountryOrderByIdDesc(CITY, COUNTRY))
                .thenReturn(Optional.empty());
        when(restTemplate.getForEntity(anyString(), eq(WeatherDataResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "NOT FOUND"));

        // When & Then
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                weatherService.getWeatherData(CITY, COUNTRY));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        assertTrue(exception.getMessage().contains("NOT FOUND"));
        verify(weatherRepository, times(1)).findTopByCityAndCountryOrderByIdDesc(CITY, COUNTRY);
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(WeatherDataResponse.class));
    }

    @Test
    void testGeneralException_FetchFromAPI() {
        when(weatherRepository.findTopByCityAndCountryOrderByIdDesc(CITY, COUNTRY))
                .thenReturn(Optional.empty());
        when(restTemplate.getForEntity(anyString(), eq(WeatherDataResponse.class)))
                .thenThrow(new RuntimeException("An error occurred while fetching weather data"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                weatherService.getWeatherData(CITY, COUNTRY));

        assertTrue(exception.getMessage().contains("An error occurred"));
        verify(weatherRepository, times(1)).findTopByCityAndCountryOrderByIdDesc(CITY, COUNTRY);
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(WeatherDataResponse.class));
    }

    @Test
    void testEmptyWeatherData_FetchFromAPI() {
        WeatherDataResponse weatherDataResponse = new WeatherDataResponse();
        ResponseEntity<WeatherDataResponse> responseEntity = new ResponseEntity<>(weatherDataResponse, HttpStatus.OK);
        when(weatherRepository.findTopByCityAndCountryOrderByIdDesc(CITY, COUNTRY))
                .thenReturn(Optional.empty());
        when(restTemplate.getForEntity(anyString(), eq(WeatherDataResponse.class)))
                .thenReturn(responseEntity);

        Exception exception = assertThrows(RuntimeException.class, () ->
                weatherService.getWeatherData(CITY, COUNTRY));

        assertTrue(exception.getMessage().contains("Weather data not available"));
    }

    @Test
    void testNullWeatherResponse_FetchFromAPI() {
        ResponseEntity<WeatherDataResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(weatherRepository.findTopByCityAndCountryOrderByIdDesc(CITY, COUNTRY))
                .thenReturn(Optional.empty());
        when(restTemplate.getForEntity(anyString(), eq(WeatherDataResponse.class)))
                .thenReturn(responseEntity);

        Exception exception = assertThrows(RuntimeException.class, () ->
                weatherService.getWeatherData(CITY, COUNTRY));

        assertTrue(exception.getMessage().contains("Weather data not available"));
    }

    @Test
    void testEmptyWeatherInfoInResponse_FetchFromAPI() {
        WeatherDataResponse response = new WeatherDataResponse();
        response.setWeather(List.of());
        ResponseEntity<WeatherDataResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        when(weatherRepository.findTopByCityAndCountryOrderByIdDesc(CITY, COUNTRY))
                .thenReturn(Optional.empty());
        when(restTemplate.getForEntity(anyString(), eq(WeatherDataResponse.class)))
                .thenReturn(responseEntity);

        Exception exception = assertThrows(RuntimeException.class, () ->
                weatherService.getWeatherData(CITY, COUNTRY));

        assertTrue(exception.getMessage().contains("Weather data not available"));
    }

    @Test
    void testBuildWeatherApiUrl_invalidUrl() {
        ReflectionTestUtils.setField(weatherService, "openWeatherUrl", "");
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                weatherService.getWeatherData(CITY, COUNTRY));

        assertTrue(exception.getMessage().contains("Base URL must not be empty"));
    }

    @Test
    void testBuildWeatherApiUrl_invalidApiKey() {
        ReflectionTestUtils.setField(weatherService, "openWeatherApiKey", "");
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                weatherService.getWeatherData(CITY, COUNTRY));

        assertTrue(exception.getMessage().contains("API Key must not be empty"));
    }

    @Test
    public void whenInputIsBlank_thenThrowsException() {
        // Assert
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> weatherService.getWeatherData("", COUNTRY));
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> weatherService.getWeatherData(CITY, ""));
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> weatherService.getWeatherData("", ""));
        Exception exception4 = assertThrows(IllegalArgumentException.class, () -> weatherService.getWeatherData("   ", "   "));
        Exception exception5 = assertThrows(IllegalArgumentException.class, () -> weatherService.getWeatherData(null, null));

        assertTrue(exception1.getMessage().contains("Input cannot be blank or null"));
        assertTrue(exception2.getMessage().contains("Input cannot be blank or null"));
        assertTrue(exception3.getMessage().contains("Input cannot be blank or null"));
        assertTrue(exception4.getMessage().contains("Input cannot be blank or null"));
        assertTrue(exception5.getMessage().contains("Input cannot be blank or null"));
    }

}