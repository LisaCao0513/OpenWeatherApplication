package com.lisa.openweather.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ApiKeyService {
    private static final int MAX_REQUESTS_PER_HOUR = 5;
    private static final int ONE_HOUR = 3600000;
    final Map<String, AtomicInteger> apiKeyUsage = new ConcurrentHashMap<>();

    @Value("${weather.api.keys}")
    String[] apiKeys;

    @PostConstruct
    public void init() {
        // Initialize the request count map
        if (apiKeys != null && apiKeys.length > 0) {
            Arrays.stream(apiKeys)
                    .forEach(apiKey -> apiKeyUsage.put(apiKey, new AtomicInteger(0)));
        } else {
            throw new IllegalArgumentException("No API keys found.");
        }
    }

    //  manage these API keys and rate limiting
    public boolean validateApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalArgumentException("API key is empty.");
        }
        return apiKeyUsage.containsKey(apiKey) && apiKeyUsage.get(apiKey).incrementAndGet() <= MAX_REQUESTS_PER_HOUR;
    }

    // Reset the counter every hour
    @Scheduled(fixedRate = ONE_HOUR)
    public void resetApiKeyUsage() {
        apiKeyUsage.forEach((key, count) -> count.set(0));
    }
}

