package org.example.forsapidev.security.access;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IRoleAccessService;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.security.jwt.JwtUtils;
import org.example.forsapidev.security.services.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Applique la matrice d'accès par zone API pour les rôles {@link ERole#AGENT} et {@link ERole#CLIENT}.
 * Les administrateurs ne sont pas filtrés ici (voir {@link IRoleAccessService#isServletPathPermitted}).
 */
@RequiredArgsConstructor
public class RoleResourceAccessFilter extends OncePerRequestFilter {

  private static final AntPathMatcher pathMatcher = new AntPathMatcher();

  private final JwtUtils jwtUtils;
  private final IRoleAccessService roleAccessService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getServletPath();
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      filterChain.doFilter(request, response);
      return;
    }
    if (isPublicPath(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      filterChain.doFilter(request, response);
      return;
    }
    if (!(auth.getPrincipal() instanceof UserDetailsImpl principal)) {
      filterChain.doFilter(request, response);
      return;
    }

    ERole role = principal.getAppRole();
    if (!roleAccessService.isServletPathPermitted(role, path)) {
      response.sendError(
          HttpServletResponse.SC_FORBIDDEN,
          "L'accès à cette zone de l'API est désactivé pour votre rôle.");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isPublicPath(String path) {
    for (String pattern : jwtUtils.AUTH_WHITELIST) {
      if (pathMatcher.match(pattern, path)) {
        return true;
      }
    }
    return false;
  }
}
