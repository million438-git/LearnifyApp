package com.million.learnify.studentbackend.security;

public record FirebasePrincipal(String uid, String email, boolean emailVerified, boolean admin) {
}
