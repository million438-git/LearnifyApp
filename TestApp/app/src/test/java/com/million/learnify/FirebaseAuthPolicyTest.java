package com.million.learnify;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FirebaseAuthPolicyTest {

    @Test
    public void verifiedStudentCanStartSession() {
        assertTrue(FirebaseAuthPolicy.hasVerifiedEmail(true));
    }

    @Test
    public void unverifiedStudentCannotStartSession() {
        assertFalse(FirebaseAuthPolicy.hasVerifiedEmail(false));
    }

    @Test
    public void adminSessionRequiresVerifiedEmailAndServerClaim() {
        assertTrue(FirebaseAuthPolicy.hasAdminAuthorization(true, true));
        assertFalse(FirebaseAuthPolicy.hasAdminAuthorization(false, true));
        assertFalse(FirebaseAuthPolicy.hasAdminAuthorization(true, false));
        assertFalse(FirebaseAuthPolicy.hasAdminAuthorization(true, null));
    }
}
