package com.million.learnify;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

final class FirebaseAuthTokenInterceptor implements Interceptor {

    private final BackendTokenProvider tokenProvider;

    FirebaseAuthTokenInterceptor(BackendTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = tokenProvider.getIdToken(false);
        Response response = chain.proceed(withToken(original, token));
        if (response.code() != 401) {
            return response;
        }

        response.close();
        String refreshedToken;
        try {
            refreshedToken = tokenProvider.getIdToken(true);
        } catch (IOException e) {
            tokenProvider.signOut();
            throw e;
        }

        Response retry = chain.proceed(withToken(original, refreshedToken));
        if (retry.code() == 401) {
            tokenProvider.signOut();
        }
        return retry;
    }

    private Request withToken(Request request, String token) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
    }
}
