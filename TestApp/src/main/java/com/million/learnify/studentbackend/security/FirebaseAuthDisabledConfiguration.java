package com.million.learnify.studentbackend.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "learnify.firebase.enabled", havingValue = "false", matchIfMissing = true)
public class FirebaseAuthDisabledConfiguration {

    @Bean
    FirebaseTokenVerifier disabledFirebaseTokenVerifier() {
        return idToken -> {
            throw new FirebaseAuthUnavailableException("Firebase authentication is not configured.");
        };
    }
}
