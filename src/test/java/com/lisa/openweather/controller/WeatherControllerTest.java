package com.lisa.openweather.controller;

import com.lisa.openweather.service.ApiKeyService;
import com.lisa.openweather.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.lisa.openweather.Utils.TestConstants.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @MockBean
    private ApiKeyService apiKeyService;
    
    @Test
    void whenApiKeyIsInvalid_thenReturnForbiddenResponse() throws Exception {
        when(apiKeyService.validateApiKey(anyString())).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get(API_URI)
                        .param("city", CITY)
                        .param("country", COUNTRY)
                        .header("X-Api-Key", INVALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMsg").value("Invalid API key or limit exceeded. Max 5 api calls an hour."))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void whenApiKeyIsValid_thenFetchWeatherSuccessfully() throws Exception {
        when(apiKeyService.validateApiKey(anyString())).thenReturn(true);
        when(weatherService.getWeatherData(CITY, COUNTRY)).thenReturn(DESCRIPTION_SUNNY);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/weather")
                        .param("city", CITY)
                        .param("country", COUNTRY)
                        .header("X-Api-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value(DESCRIPTION_SUNNY))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.errorMsg").isEmpty());
    }

    @Test
    void whenParametersAreMissing_thenHandleMissingServletRequestParameterException() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(API_URI)
                        .param("city", CITY)
                        .header("X-Api-Key", API_KEY)// missing 'country'
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMsg").value("Custom Error: 'country' parameter is missing."))
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(MockMvcRequestBuilders.get(API_URI)
                    .param("country", COUNTRY)
                    .header("X-Api-Key", API_KEY)  // missing 'city'
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMsg").value("Custom Error: 'city' parameter is missing."))
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(MockMvcRequestBuilders.get(API_URI)
                        .param("city", CITY)
                        .param("country", COUNTRY)// missing 'apiKey' header
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMsg").value("Custom Error: 'X-Api-Key' header is missing."))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void whenWeatherServiceThrowsException_thenReturnErrorMessage() throws Exception {
        when(apiKeyService.validateApiKey(anyString())).thenReturn(true);
        when(weatherService.getWeatherData(anyString(), anyString())).thenThrow(new RuntimeException("Weather data not available"));

        mockMvc.perform(MockMvcRequestBuilders.get(API_URI)
                        .param("city", CITY)
                        .param("country", COUNTRY)
                        .header("X-Api-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMsg").value("Weather data not available"))
                .andExpect(jsonPath("$.success").value(false));
    }
}