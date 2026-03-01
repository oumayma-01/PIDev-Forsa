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

    logger.error("Unauthorized error: {}", authException.getMessage());

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String message = authException.getMessage();
    if (message == null || message.isBlank()) {
      message = "Full authentication is required to access this resource";
    }

    String jsonResponse = """
                {
                  "timestamp": "%s",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "%s",
                  "path": "%s"
                }
                """.formatted(
            LocalDateTime.now(),
            message,
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String jsonResponse = """
            {
              "timestamp": "%s",
              "status": 401,
              "error": "Unauthorized",
              "message": "%s",
              "path": "%s"
            }
            """.formatted(
            LocalDateTime.now(),
            authException.getMessage(),
            request.getRequestURI()
    );

    response.getWriter().write(jsonResponse);
  }
}
}
