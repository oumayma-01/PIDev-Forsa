package org.example.forsapidev.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

  private static final Logger logger =
          LoggerFactory.getLogger(AuthEntryPointJwt.class);

  @Override
  public void commence(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException)
          throws IOException {

    logger.error("Unauthorized error: {}", authException == null ? "unknown" : authException.getMessage());

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String message = (authException == null || authException.getMessage() == null || authException.getMessage().isBlank())
            ? "Full authentication is required to access this resource"
            : authException.getMessage();

    String jsonResponse = String.format(
            "{\n  \"timestamp\": \"%s\",\n  \"status\": 401,\n  \"error\": \"Unauthorized\",\n  \"message\": \"%s\",\n  \"path\": \"%s\"\n}",
            LocalDateTime.now(),
            escapeJson(message),
            request.getRequestURI()
    );

    response.getWriter().write(jsonResponse);
  }

  // Petit utilitaire pour échapper les guillemets simples dans le message
  private String escapeJson(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
  }
}
