package com.million.learnify.studentbackend.controller;

import com.million.learnify.studentbackend.dto.AskRequest;
import com.million.learnify.studentbackend.dto.AskResponse;
import com.million.learnify.studentbackend.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.million.learnify.studentbackend.security.FirebasePrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssistantControllerTest {

    @Mock
    private GeminiService mockGeminiService;

    private AssistantController controller;

    @BeforeEach
    void setUp() {
        controller = new AssistantController(mockGeminiService);
    }

    @Test
    @DisplayName("Should return answer from GeminiService when valid question provided")
    void testAsk_Success() {
        AskRequest req = new AskRequest();
        req.setQuestion("Explain Newton's First Law");

        String expectedAnswer = "Topic Overview\nAn object remains at rest unless acted on by an external force.";
        when(mockGeminiService.askGemini("Explain Newton's First Law")).thenReturn(expectedAnswer);

        AskResponse response = controller.ask(req);

        assertNotNull(response);
        assertEquals(expectedAnswer, response.getAnswer());
        verify(mockGeminiService, times(1)).askGemini("Explain Newton's First Law");
    }

    @Test
    @DisplayName("Should return prompt when request is null or empty")
    void testAsk_NullOrEmptyRequest() {
        AskResponse nullReqRes = controller.ask(null);
        assertNotNull(nullReqRes);
        assertEquals("Please ask an academic question to get started.", nullReqRes.getAnswer());

        AskRequest emptyReq = new AskRequest();
        emptyReq.setQuestion("   ");
        AskResponse emptyRes = controller.ask(emptyReq);
        assertNotNull(emptyRes);
        assertEquals("Please ask an academic question to get started.", emptyRes.getAnswer());

        verifyNoInteractions(mockGeminiService);
    }

    @Test
    @DisplayName("Should catch unexpected exceptions and return safe response")
    void testAsk_ExceptionHandler() {
        AskRequest req = new AskRequest();
        req.setQuestion("Cause unexpected crash");

        when(mockGeminiService.askGemini("Cause unexpected crash")).thenThrow(new RuntimeException("Simulated unexpected crash"));

        AskResponse response = controller.ask(req);

        assertNotNull(response);
        assertTrue(response.getAnswer().contains("EduAI Error"));
        assertFalse(response.getAnswer().contains("Simulated unexpected crash"), "Internal exception details must be masked");
    }
    
@Test
@DisplayName("Rate limits each authenticated Firebase user separately")
void rateLimitIsIsolatedPerFirebaseUser() {
    AskRequest request = new AskRequest();
    request.setQuestion("Explain gravity");

    when(mockGeminiService.askGemini("Explain gravity"))
            .thenReturn("Gravity is an attractive force.");

    Authentication studentOne = new UsernamePasswordAuthenticationToken(
            new FirebasePrincipal("student-one", "one@example.com", true, false),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));

    Authentication studentTwo = new UsernamePasswordAuthenticationToken(
            new FirebasePrincipal("student-two", "two@example.com", true, false),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));

    // Student one uses all 30 allowed requests.
    for (int i = 0; i < 30; i++) {
        assertEquals(
                "Gravity is an attractive force.",
                controller.ask(request, studentOne).getAnswer());
    }

    // Student one's next request is rate-limited.
    assertTrue(controller.ask(request, studentOne)
            .getAnswer().contains("rate limit"));

    // Student two should still be able to use the assistant.
    assertEquals(
            "Gravity is an attractive force.",
            controller.ask(request, studentTwo).getAnswer());

    verify(mockGeminiService, times(31))
            .askGemini("Explain gravity");
}

}

