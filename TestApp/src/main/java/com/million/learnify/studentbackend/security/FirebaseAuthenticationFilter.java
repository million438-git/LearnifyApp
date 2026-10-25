package com.million.learnify.studentbackend.security;

import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public final class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private static final int MAX_TOKEN_LENGTH = 8192;

    private final FirebaseTokenVerifier tokenVerifier;

    public FirebaseAuthenticationFilter(FirebaseTokenVerifier tokenVerifier) {
        this.tokenVerifier = tokenVerifier;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorization.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bearer authentication is required.");
            return;
        }

        String idToken = authorization.substring("Bearer ".length()).trim();
        if (idToken.isEmpty() || idToken.length() > MAX_TOKEN_LENGTH) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication token.");
            return;
        }

        try {
            FirebasePrincipal principal = tokenVerifier.verify(idToken);
            if (!principal.emailVerified()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "A verified email account is required.");
                return;
            }

            String authority = principal.admin() ? "ROLE_ADMIN" : "ROLE_STUDENT";
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority(authority)));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (FirebaseAuthUnavailableException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.getMessage());
        } catch (FirebaseAuthException e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired authentication token.");
        }
    }
}
