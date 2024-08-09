package com.lisa.openweather.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @InjectMocks
    private ApiKeyService apiKeyService;

    // Set up the environment to simulate @Value injection
    @BeforeEach
    void setUp() {
        apiKeyService.apiKeys = new String[]{"key1", "key2", "key3"};
        apiKeyService.init();  // Manually call to simulate @PostConstruct
    }

    @Test
    public void testApiKeysInitialization_success() {
        // Test that all keys are initialized with a count of 0
        assertTrue(apiKeyService.apiKeyUsage.containsKey("key1"));
        assertTrue(apiKeyService.apiKeyUsage.containsKey("key2"));
        assertTrue(apiKeyService.apiKeyUsage.containsKey("key3"));
        assertEquals(0, apiKeyService.apiKeyUsage.get("key1").get());
        assertEquals(0, apiKeyService.apiKeyUsage.get("key2").get());
        assertEquals(0, apiKeyService.apiKeyUsage.get("key3").get());
    }

    @Test
    public void testApiKeysInitialization_withEmptyApiKeys() {
        //api keys is null
        apiKeyService.apiKeys = null;
        assertThrows(IllegalArgumentException.class, () -> apiKeyService.init());
        //api keys are empty
        apiKeyService.apiKeys = new String[]{};
        assertThrows(IllegalArgumentException.class, () -> apiKeyService.init());
    }

    @Test
    public void testValidateApiKey() {
        // Validate the API key increment and check within limit
        assertTrue(apiKeyService.validateApiKey("key1"));  // 1st request
        assertTrue(apiKeyService.validateApiKey("key1"));  // 2nd request
        assertTrue(apiKeyService.validateApiKey("key1"));  // 3rd request
        assertTrue(apiKeyService.validateApiKey("key1"));  // 4th request
        assertTrue(apiKeyService.validateApiKey("key1"));  // 5th request
        assertFalse(apiKeyService.validateApiKey("key1")); // 6th request, should fail
    }

    @Test
    public void testValidateApiKey_withInvalidApiKey() {
        // Validate the API key increment and check within limit
        assertFalse(apiKeyService.validateApiKey("key5"));
        assertThrows(IllegalArgumentException.class, () ->apiKeyService.validateApiKey(""));
        assertThrows(IllegalArgumentException.class, () ->apiKeyService.validateApiKey(null));
    }

    @Test
    public void testResetApiKeyUsage() {
        // Simulate usage
        apiKeyService.apiKeyUsage.get("key1").getAndIncrement();  // 1 request
        apiKeyService.apiKeyUsage.get("key2").getAndIncrement();  // 1 request

        // Reset usage
        apiKeyService.resetApiKeyUsage();

        // Test reset
        assertEquals(0, apiKeyService.apiKeyUsage.get("key1").get());
        assertEquals(0, apiKeyService.apiKeyUsage.get("key2").get());
    }
}