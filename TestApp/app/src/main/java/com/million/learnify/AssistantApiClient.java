package com.million.learnify;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

final class AssistantApiClient {

    private AssistantApiClient() {
    }

    static AIChatActivity.AssistantService create(
            String baseUrl,
            boolean debug,
            BackendTokenProvider tokenProvider) {
        validateBaseUrl(baseUrl, debug);
        if (tokenProvider == null) {
            throw new IllegalArgumentException("A Firebase authentication provider is required.");
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new FirebaseAuthTokenInterceptor(tokenProvider))
                .build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AIChatActivity.AssistantService.class);
    }

    static void validateBaseUrl(String baseUrl, boolean debug) {
        HttpUrl parsedUrl = baseUrl == null ? null : HttpUrl.parse(baseUrl);
        if (parsedUrl == null
                || !baseUrl.endsWith("/")
                || !parsedUrl.username().isEmpty()
                || !parsedUrl.password().isEmpty()
                || parsedUrl.encodedQuery() != null
                || parsedUrl.encodedFragment() != null) {
            throw new IllegalArgumentException(
                    "AI service is not configured. Set LEARNIFY_API_BASE_URL to a valid service URL ending in '/'.");
        }

        String host = parsedUrl.host();
        boolean localHost = "localhost".equals(host)
                || "127.0.0.1".equals(host)
                || "10.0.2.2".equals(host);
        boolean privateLanHost = isPrivateIpv4(host);
        if (debug) {
            if (!"https".equals(parsedUrl.scheme())
                    && !("http".equals(parsedUrl.scheme()) && (localHost || privateLanHost))) {
                throw new IllegalArgumentException(
                        "Debug AI service URLs must use HTTPS or a permitted local development host.");
            }
        } else if (!"https".equals(parsedUrl.scheme()) || localHost) {
            throw new IllegalArgumentException(
                    "Release AI service URL must use HTTPS and must not point to a local development host.");
        }
    }

    private static boolean isPrivateIpv4(String host) {
        if (host == null || !host.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
            return false;
        }
        String[] parts = host.split("\\.");
        int[] octets = new int[4];
        for (int i = 0; i < 4; i++) {
            try {
                octets[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                return false;
            }
            if (octets[i] < 0 || octets[i] > 255) {
                return false;
            }
        }
        return octets[0] == 10
                || (octets[0] == 172 && octets[1] >= 16 && octets[1] <= 31)
                || (octets[0] == 192 && octets[1] == 168);
    }
}
