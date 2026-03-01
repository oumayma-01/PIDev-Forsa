package org.example.forsapidev.security;

import lombok.NoArgsConstructor;
import org.example.forsapidev.security.jwt.AuthAccessDeniedHandler;
import org.example.forsapidev.security.jwt.AuthEntryPointJwt;
import org.example.forsapidev.security.jwt.AuthTokenFilter;
import org.example.forsapidev.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@NoArgsConstructor
public class WebSecurityConfig implements WebMvcConfigurer {

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Autowired
  private AuthAccessDeniedHandler accessDeniedHandler;

  @Autowired
  private UserDetailsServiceImpl userDetailsService;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
          HttpSecurity http
  ) throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder())
            .and()
            .build();
  }

  @Bean
  protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
            .cors(cors -> cors.disable()) // ou config CORS custom si besoin
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(unauthorizedHandler)
                    .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                    // ENDPOINTS PUBLICS (SANS TOKEN)
                    .requestMatchers(
                            "/api/auth/**",      // signup, signin, reset...
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html"
                    ).permitAll()
                    // TOUT LE RESTE NÉCESSITE UN TOKEN VALIDE
                    .anyRequest().authenticated()
            )
            // Filtre JWT avant UsernamePasswordAuthenticationFilter
            .addFilterBefore(authenticationJwtTokenFilter(),
                    UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    // Ici on ignore seulement les ressources statiques de Swagger,
    // PAS les endpoints /api/** (ils doivent passer par la sécurité JWT)
    return (web) -> web.ignoring()
            .requestMatchers(
                    "/swagger-ui.html",
                    "/webjars/**",
                    "/v3/api-docs/**"
            );
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }
}
