package org.example.forsapidev.security;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.example.forsapidev.security.jwt.AuthEntryPointJwt;
import org.example.forsapidev.security.jwt.AuthTokenFilter;
import org.example.forsapidev.security.jwt.JwtUtils;
import org.example.forsapidev.security.services.UserDetailsServiceImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
@NoArgsConstructor
public class WebSecurityConfig implements WebMvcConfigurer {

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Autowired
  private JwtUtils securityUtils;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http, UserDetailsServiceImpl userDetailsService)
          throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder())
            .and()
            .build();
  }

  @Bean
  protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors().and().csrf().disable();
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    http.exceptionHandling().authenticationEntryPoint(unauthorizedHandler);

    http.authorizeHttpRequests()
            .requestMatchers(securityUtils.AUTH_WHITELIST).permitAll()
            .requestMatchers("/api/scoring/**").permitAll()
            .requestMatchers("/api/recommendations/**").permitAll()
            .requestMatchers("/api/rerating/**").permitAll()
            .requestMatchers("/api/partners/**").permitAll()
            .requestMatchers("/api/partner-transactions/**").permitAll()
            .requestMatchers("/api/qr-code/**").permitAll()
            .requestMatchers("/api/partner-reviews/**").permitAll()
            .requestMatchers("/api/partner-analytics/**").permitAll()
            .requestMatchers("/api/fraud-alerts/**").permitAll()
            .requestMatchers("/api/cashback/**").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/swagger-ui.html").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()
            .requestMatchers("/api-docs/**").permitAll()
            .anyRequest().authenticated();

    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() throws Exception {
    return (web) -> web.ignoring()
            .requestMatchers(securityUtils.AUTH_WHITELIST)
            .requestMatchers("/swagger-ui/**")
            .requestMatchers("/swagger-ui.html")
            .requestMatchers("/v3/api-docs/**")
            .requestMatchers("/api-docs/**");
  }


  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }
}