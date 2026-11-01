package com.million.learnify;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * SettingsApiClient - Network Client Factory for Settings & Governance APIs.
 */
public final class SettingsApiClient {

    private SettingsApiClient() {
    }

    public static SettingsApiService create(
            String baseUrl,
            final BackendTokenProvider tokenProvider) {

        if (baseUrl == null || !baseUrl.endsWith("/")) {
            baseUrl = BuildConfig.DEBUG ? "http://10.0.2.2:8080/" : "https://api.learnify.org/";
        }

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS);

        if (tokenProvider != null) {
            clientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    String token = tokenProvider.getIdToken(false);
                    Request.Builder reqBuilder = chain.request().newBuilder();
                    if (token != null && !token.trim().isEmpty()) {
                        reqBuilder.header("Authorization", "Bearer " + token.trim());
                    }
                    return chain.proceed(reqBuilder.build());
                }
            });
        }

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SettingsApiService.class);
    }
}
