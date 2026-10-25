package com.million.learnify;

import java.io.IOException;

final class SessionAuthenticationException extends IOException {
    SessionAuthenticationException(String message) {
        super(message);
    }

    SessionAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
