package com.million.learnify.studentbackend.security;

import com.google.firebase.auth.FirebaseAuthException;

public interface FirebaseTokenVerifier {
    FirebasePrincipal verify(String idToken) throws FirebaseAuthException;
}
