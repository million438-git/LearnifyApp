package com.million.learnify;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class FirebaseBackendTokenProvider implements BackendTokenProvider {

    private final Context context;
    private final FirebaseAuth auth;

    FirebaseBackendTokenProvider(Context context) {
        this.context = context.getApplicationContext();
        this.auth = FirebaseAuthSupport.getConfiguredAuth(context);
        if (auth == null) {
            throw new IllegalStateException("Firebase Android configuration is missing.");
        }
    }

    @Override
    public String getIdToken(boolean forceRefresh) throws IOException {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            throw new SessionAuthenticationException("Sign in to use online Learnify services.");
        }

        try {
            GetTokenResult result = Tasks.await(user.getIdToken(forceRefresh), 20, TimeUnit.SECONDS);
            if (result == null || TextUtils.isEmpty(result.getToken())) {
                throw new SessionAuthenticationException("A secure sign-in token is unavailable.");
            }
            return result.getToken();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SessionAuthenticationException("Sign-in was interrupted. Please try again.", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new SessionAuthenticationException("Your secure sign-in has expired. Sign in again.", e);
        }
    }

    @Override
    public void signOut() {
        FirebaseSessionManager.signOut(context, auth);
    }
}
