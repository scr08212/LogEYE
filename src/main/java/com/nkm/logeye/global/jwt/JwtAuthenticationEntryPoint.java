package com.nkm.logeye.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.error("Authentication Error: {}", authException.getMessage());

        String errorMessage = authException.getMessage();
        String errorCode = "AUTHENTICATION_FAILED";

        if(authException.getCause() instanceof JwtException){
            errorMessage = authException.getCause().getMessage();
            errorCode = "INVALID_TOKEN";
        }

        sendErrorReport(response, errorCode, errorMessage);
    }

    private void sendErrorReport(HttpServletResponse response, String errorCode, String errorMessage) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("errorCode", errorCode);
        body.put("errorMessage", errorMessage);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}