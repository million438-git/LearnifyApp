package com.million.learnify;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

final class FirebaseAuthSupport {

    private FirebaseAuthSupport() {
    }

    static FirebaseAuth getConfiguredAuth(Context context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            return null;
        }
        return FirebaseAuth.getInstance();
    }

    static String messageFor(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) exception).getErrorCode();
            if ("ERROR_NETWORK_REQUEST_FAILED".equals(code)) {
                return "Network connection unavailable. Please try again.";
            }
            if ("ERROR_TOO_MANY_REQUESTS".equals(code)) {
                return "Too many attempts. Please wait before trying again.";
            }
            if ("ERROR_EMAIL_ALREADY_IN_USE".equals(code)) {
                return "An account already exists for this email. Sign in or reset its password.";
            }
            if ("ERROR_INVALID_EMAIL".equals(code)) {
                return "Enter a valid email address.";
            }
            if ("ERROR_USER_DISABLED".equals(code)) {
                return "This account is disabled. Contact Learnify support.";
            }
            if ("ERROR_WRONG_PASSWORD".equals(code)
                    || "ERROR_INVALID_CREDENTIAL".equals(code)
                    || "ERROR_USER_NOT_FOUND".equals(code)) {
                return "Email or password is incorrect.";
            }
        }
        return "Unable to authenticate right now. Please try again.";
    }
}
