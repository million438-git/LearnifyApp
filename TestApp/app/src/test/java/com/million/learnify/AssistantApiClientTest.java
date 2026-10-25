package com.million.learnify;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class AssistantApiClientTest {

    @Test
    public void acceptsLoopbackHttpForDebugBuilds() {
        AssistantApiClient.validateBaseUrl("http://10.0.2.2:8080/", true);
        AssistantApiClient.validateBaseUrl("http://localhost:8080/", true);
    }

    @Test
    public void acceptsHttpsForReleaseBuilds() {
        AssistantApiClient.validateBaseUrl("https://api.example.com/", false);
    }

    @Test
    public void rejectsUnconfiguredBaseUrl() {
        assertThrows(IllegalArgumentException.class,
                () -> AssistantApiClient.validateBaseUrl("", false));
    }

    @Test
    public void rejectsLocalOrCleartextReleaseEndpoints() {
        assertThrows(IllegalArgumentException.class,
                () -> AssistantApiClient.validateBaseUrl("http://api.example.com/", false));
        assertThrows(IllegalArgumentException.class,
                () -> AssistantApiClient.validateBaseUrl("https://localhost:8080/", false));
    }

    @Test
    public void rejectsNonLocalCleartextDebugEndpoints() {
        assertThrows(IllegalArgumentException.class,
                () -> AssistantApiClient.validateBaseUrl("http://api.example.com/", true));
    }

    @Test
    public void requiresTrailingSlashForRetrofitBaseUrl() {
        assertThrows(IllegalArgumentException.class,
                () -> AssistantApiClient.validateBaseUrl("https://api.example.com", false));
    }
}
