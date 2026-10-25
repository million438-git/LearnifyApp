package com.million.learnify.studentbackend.security;

public final class FirebaseAuthUnavailableException extends RuntimeException {
    public FirebaseAuthUnavailableException(String message) {
        super(message);
    }
}
