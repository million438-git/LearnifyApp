package com.million.learnify.studentbackend.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "learnify.firebase.enabled", havingValue = "true")
public class FirebaseAdminConfiguration {

    @Bean(destroyMethod = "delete")
    FirebaseApp firebaseApp(@Value("${learnify.firebase.project-id}") String projectId) throws IOException {
        if (projectId.isBlank()) {
            throw new IllegalStateException("FIREBASE_PROJECT_ID must be configured when Firebase authentication is enabled.");
        }

        List<FirebaseApp> apps = FirebaseApp.getApps();
        for (FirebaseApp app : apps) {
            if ("learnify".equals(app.getName())) {
                return app;
            }
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setProjectId(projectId)
                .build();
        return FirebaseApp.initializeApp(options, "learnify");
    }

    @Bean
    FirebaseTokenVerifier firebaseTokenVerifier(FirebaseApp firebaseApp) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(firebaseApp);
        return idToken -> {
            FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken, true);
            Map<String, Object> claims = decoded.getClaims();
            boolean emailVerified = Boolean.TRUE.equals(claims.get("email_verified"));
            boolean admin = Boolean.TRUE.equals(claims.get("admin"));
            return new FirebasePrincipal(
                    decoded.getUid(),
                    decoded.getEmail(),
                    emailVerified,
                    admin);
        };
    }
}
