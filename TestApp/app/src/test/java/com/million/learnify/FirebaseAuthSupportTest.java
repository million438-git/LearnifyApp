package com.million.learnify;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FirebaseAuthSupportTest {

    @Test
    public void testNullReturnsFallback() {
        String msg = FirebaseAuthSupport.messageFor(null);
        assertNotNull(msg);
        assertTrue(msg.contains("Unable to authenticate"));
    }

    @Test
    public void testOperationNotAllowedExplainsFirebaseConsole() {
        FirebaseAuthException ex = new FirebaseAuthException("ERROR_OPERATION_NOT_ALLOWED", "Operation not allowed");
        String msg = FirebaseAuthSupport.messageFor(ex);
        assertNotNull(msg);
        assertTrue(msg.contains("Email and password sign-in is disabled") || msg.contains("Firebase Console"));
    }

    @Test
    public void testWeakPasswordExceptionMessage() {
        FirebaseAuthWeakPasswordException ex = new FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "Weak", "Password should be at least 6 characters");
        String msg = FirebaseAuthSupport.messageFor(ex);
        assertNotNull(msg);
        assertTrue(msg.contains("Password is too weak"));
    }

    @Test
    public void testUserCollisionExceptionMessage() {
        FirebaseAuthUserCollisionException ex = new FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "Collision");
        String msg = FirebaseAuthSupport.messageFor(ex);
        assertNotNull(msg);
        assertTrue(msg.contains("already exists"));
    }

    @Test
    public void testNetworkExceptionMessage() {
        FirebaseNetworkException ex = new FirebaseNetworkException("Network request failed");
        String msg = FirebaseAuthSupport.messageFor(ex);
        assertNotNull(msg);
        assertTrue(msg.contains("Network connection unavailable"));
    }

    @Test
    public void testTooManyRequestsExceptionMessage() {
        FirebaseTooManyRequestsException ex = new FirebaseTooManyRequestsException("Too many requests");
        String msg = FirebaseAuthSupport.messageFor(ex);
        assertNotNull(msg);
        assertTrue(msg.contains("Too many attempts"));
    }

    @Test
    public void testInvalidEmailMessage() {
        FirebaseAuthException ex = new FirebaseAuthException("ERROR_INVALID_EMAIL", "Bad email");
        String msg = FirebaseAuthSupport.messageFor(ex);
        assertNotNull(msg);
        assertTrue(msg.contains("valid email"));
    }

    @Test
    public void testUserDisabledMessage() {
        FirebaseAuthException ex = new FirebaseAuthException("ERROR_USER_DISABLED", "Disabled");
        String msg = FirebaseAuthSupport.messageFor(ex);
        assertNotNull(msg);
        assertTrue(msg.contains("disabled"));
    }

    @Test
    public void testWrongPasswordMessage() {
        FirebaseAuthException ex = new FirebaseAuthException("ERROR_WRONG_PASSWORD", "Wrong pass");
        String msg = FirebaseAuthSupport.messageFor(ex);
        assertNotNull(msg);
        assertTrue(msg.toLowerCase().contains("incorrect"));
    }

    @Test
    public void testUserNotFoundMessage() {
        FirebaseAuthException ex = new FirebaseAuthException("ERROR_USER_NOT_FOUND", "Not found");
        String msg = FirebaseAuthSupport.messageFor(ex);
        assertNotNull(msg);
        assertTrue(msg.contains("No Learnify account"));
    }

    @Test
    public void testGenericFirebaseNetworkFallback() {
        FirebaseException ex = new FirebaseException("A network error has occurred.");
        String msg = FirebaseAuthSupport.messageFor(ex);
        assertNotNull(msg);
        assertTrue(msg.contains("Network connection unavailable"));
    }
}

