package com.million.learnify;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

final class FirebaseAuthSupport {

    private static final String TAG = "FirebaseAuthSupport";

    private FirebaseAuthSupport() {
    }

    static FirebaseAuth getConfiguredAuth(Context context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            return null;
        }
        return FirebaseAuth.getInstance();
    }

    static String messageFor(Exception exception) {
        if (exception == null) {
            return "Unable to authenticate right now. Please try again.";
        }

        String code = null;
        if (exception instanceof FirebaseAuthException) {
            code = ((FirebaseAuthException) exception).getErrorCode();
        }

        // Safe diagnostic logging for developers without exposing passwords or tokens
        Log.w(TAG, "FirebaseAuth error [" + exception.getClass().getSimpleName()
                + (code != null ? ", code=" + code : "") + "]: " + exception.getMessage());

        // 1. Weak password
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            String reason = ((FirebaseAuthWeakPasswordException) exception).getReason();
            if (reason != null && !reason.trim().isEmpty()) {
                return "Password is too weak: " + reason;
            }
            return "Password is too weak. Please use at least 6 characters.";
        }
        if ("ERROR_WEAK_PASSWORD".equalsIgnoreCase(code) || "weak-password".equalsIgnoreCase(code)) {
            return "Password is too weak. Please use at least 6 characters.";
        }

        // 2. Email/Password provider disabled in Firebase Console
        if ("ERROR_OPERATION_NOT_ALLOWED".equalsIgnoreCase(code)
                || "operation-not-allowed".equalsIgnoreCase(code)) {
            return "Email and password sign-in is disabled in your Firebase project. Please enable Email/Password under Authentication > Sign-in method in the Firebase Console.";
        }

        // 3. User collision / duplicate email
        if (exception instanceof FirebaseAuthUserCollisionException
                || "ERROR_EMAIL_ALREADY_IN_USE".equalsIgnoreCase(code)
                || "email-already-in-use".equalsIgnoreCase(code)
                || "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL".equalsIgnoreCase(code)) {
            return "An account already exists for this email. Sign in or reset its password.";
        }

        // 4. Network unavailable
        if (exception instanceof FirebaseNetworkException
                || "ERROR_NETWORK_REQUEST_FAILED".equalsIgnoreCase(code)
                || "network-request-failed".equalsIgnoreCase(code)) {
            return "Network connection unavailable. Please check your internet connection and try again.";
        }

        // 5. Too many requests / rate limit
        if (exception instanceof FirebaseTooManyRequestsException
                || "ERROR_TOO_MANY_REQUESTS".equalsIgnoreCase(code)
                || "too-many-requests".equalsIgnoreCase(code)) {
            return "Too many attempts. Please wait before trying again.";
        }

        // 6. Invalid email
        if ("ERROR_INVALID_EMAIL".equalsIgnoreCase(code)
                || "invalid-email".equalsIgnoreCase(code)) {
            return "Enter a valid email address.";
        }

        // 7. Disabled user
        if ("ERROR_USER_DISABLED".equalsIgnoreCase(code)
                || "user-disabled".equalsIgnoreCase(code)) {
            return "This account is disabled. Contact Learnify support.";
        }

        // 8. User not found
        if ("ERROR_USER_NOT_FOUND".equalsIgnoreCase(code)
                || "user-not-found".equalsIgnoreCase(code)) {
            return "No Learnify account was found for this email.";
        }

        // 9. Wrong password or invalid credentials
        if ("ERROR_WRONG_PASSWORD".equalsIgnoreCase(code)
                || "wrong-password".equalsIgnoreCase(code)
                || "ERROR_INVALID_CREDENTIAL".equalsIgnoreCase(code)
                || "invalid-credential".equalsIgnoreCase(code)) {
            return "Email or password is incorrect.";
        }

        // 10. Re-authentication required
        if ("ERROR_REQUIRES_RECENT_LOGIN".equalsIgnoreCase(code)
                || "requires-recent-login".equalsIgnoreCase(code)) {
            return "This action requires recent authentication. Please sign in again.";
        }

        // 11. Project authorization / API key / certificate mismatch
        if ("ERROR_APP_NOT_AUTHORIZED".equalsIgnoreCase(code)
                || "app-not-authorized".equalsIgnoreCase(code)
                || "ERROR_API_KEY_NOT_VALID".equalsIgnoreCase(code)
                || "api-key-not-valid".equalsIgnoreCase(code)) {
            return "Firebase app authorization failed. Check project configuration and SHA-1 certificate.";
        }

        // 12. Generic Firebase network fallback check
        if (exception instanceof FirebaseException) {
            String msg = exception.getMessage();
            if (msg != null && msg.toLowerCase().contains("network")) {
                return "Network connection unavailable. Please check your internet connection and try again.";
            }
        }

        return "Unable to authenticate right now. Please try again.";
    }
}
