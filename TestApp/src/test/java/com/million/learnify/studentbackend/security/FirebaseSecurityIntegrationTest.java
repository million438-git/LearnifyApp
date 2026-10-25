package com.million.learnify.studentbackend.security;

import com.google.firebase.ErrorCode;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuthException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "learnify.firebase.enabled=false")
@AutoConfigureMockMvc
class FirebaseSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FirebaseTokenVerifier tokenVerifier;

    @Test
    void statusCheckIsPublic() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk());
    }

    @Test
    void healthCheckIsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void applicationApiRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(post("/api/assistant/ask")
                        .contentType("application/json")
                        .content("{\"question\":\"Explain gravity\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void applicationApiRejectsMalformedAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/api/assistant/ask")
                        .header(AUTHORIZATION, "Basic invalid")
                        .contentType("application/json")
                        .content("{\"question\":\"Explain gravity\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void applicationApiRejectsInvalidFirebaseToken() throws Exception {
        when(tokenVerifier.verify("invalid-token"))
                .thenThrow(new FirebaseAuthException(
                        ErrorCode.UNAUTHENTICATED,
                        "Invalid test token",
                        null,
                        null,
                        AuthErrorCode.INVALID_ID_TOKEN));

        mockMvc.perform(post("/api/assistant/ask")
                        .header(AUTHORIZATION, "Bearer invalid-token")
                        .contentType("application/json")
                        .content("{\"question\":\"Explain gravity\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void applicationApiRejectsExpiredFirebaseToken() throws Exception {
        when(tokenVerifier.verify("expired-token"))
                .thenThrow(new FirebaseAuthException(
                        ErrorCode.UNAUTHENTICATED,
                        "Expired test token",
                        null,
                        null,
                        AuthErrorCode.EXPIRED_ID_TOKEN));

        mockMvc.perform(post("/api/assistant/ask")
                        .header(AUTHORIZATION, "Bearer expired-token")
                        .contentType("application/json")
                        .content("{\"question\":\"Explain gravity\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void applicationApiRejectsRevokedFirebaseToken() throws Exception {
        when(tokenVerifier.verify("revoked-token"))
                .thenThrow(new FirebaseAuthException(
                        ErrorCode.UNAUTHENTICATED,
                        "Revoked test token",
                        null,
                        null,
                        AuthErrorCode.REVOKED_ID_TOKEN));

        mockMvc.perform(post("/api/assistant/ask")
                        .header(AUTHORIZATION, "Bearer revoked-token")
                        .contentType("application/json")
                        .content("{\"question\":\"Explain gravity\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void applicationApiAcceptsVerifiedFirebaseStudent() throws Exception {
        when(tokenVerifier.verify("student-token"))
                .thenReturn(new FirebasePrincipal("student-uid", "student@example.com", true, false));

        mockMvc.perform(post("/api/assistant/ask")
                        .header("Authorization", "Bearer student-token")
                        .contentType("application/json")
                        .content("{\"question\":\"Explain gravity\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void adminApiRejectsStudentRole() throws Exception {
        when(tokenVerifier.verify("student-token"))
                .thenReturn(new FirebasePrincipal("student-uid", "student@example.com", true, false));

        mockMvc.perform(get("/api/admin/payments")
                        .header("Authorization", "Bearer student-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminApiRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/admin/payments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminApiRequiresVerifiedAdminClaim() throws Exception {
        when(tokenVerifier.verify("admin-token"))
                .thenReturn(new FirebasePrincipal("admin-uid", "admin@example.com", true, true));

        mockMvc.perform(get("/api/admin/payments")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unverifiedFirebaseEmailIsRejected() throws Exception {
        when(tokenVerifier.verify("unverified-token"))
                .thenReturn(new FirebasePrincipal("student-uid", "student@example.com", false, false));

        mockMvc.perform(post("/api/assistant/ask")
                        .header("Authorization", "Bearer unverified-token")
                        .contentType("application/json")
                        .content("{\"question\":\"Explain gravity\"}"))
                .andExpect(status().isForbidden());
    }
}
