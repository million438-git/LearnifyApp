package com.million.learnify;

final class FirebaseAuthPolicy {

    private FirebaseAuthPolicy() {
    }

    static boolean hasVerifiedEmail(boolean emailVerified) {
        return emailVerified;
    }

    static boolean hasAdminAuthorization(boolean emailVerified, Object adminClaim) {
        return emailVerified && Boolean.TRUE.equals(adminClaim);
    }
}
