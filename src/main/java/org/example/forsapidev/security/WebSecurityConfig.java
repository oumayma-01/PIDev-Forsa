package org.example.forsapidev.security;

import lombok.NoArgsConstructor;
import org.example.forsapidev.Repositories.RoleRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.security.jwt.*;
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
import org.example.forsapidev.security.services.UserDetailsServiceImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;


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

  private JwtUtils securityUtils;
  @Autowired
  private AuthAccessDeniedHandler accessDeniedHandler;
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Bean
  public OAuth2SuccessHandler oAuth2SuccessHandler() {
    return new OAuth2SuccessHandler(userRepository, roleRepository, securityUtils);
  }
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
            .cors().and()
            .csrf().disable()

            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .and()

            .exceptionHandling()
            .authenticationEntryPoint(unauthorizedHandler)
            .accessDeniedHandler(accessDeniedHandler)
            .and()

            .authorizeHttpRequests()
            .requestMatchers(securityUtils.AUTH_WHITELIST).permitAll()
            .anyRequest().authenticated()
            .and()

            .oauth2Login()
            .successHandler(oAuth2SuccessHandler())
            .and()

            .addFilterBefore(authenticationJwtTokenFilter(),
                    UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }
}
