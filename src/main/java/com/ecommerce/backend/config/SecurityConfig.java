package com.ecommerce.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Reads the Vercel URL from your Render Environment Variables
    @Value("${app.frontend-url}")
    private String frontendUrl;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Fixed CORS logic to allow Vercel to communicate with Render
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(frontendUrl)); // Uses your injected Vercel URL
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Always allow pre-flight OPTIONS requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🎯 Explicitly permit GET for the root (Cron Job)
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Allow public access for login/registration
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders").permitAll() // Fixed: Order request will now pass CORS
                        .requestMatchers("/uploads/**").permitAll()

                        // Admin only
                        .requestMatchers("/api/products/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/categories/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/orders/**").hasAuthority("ROLE_ADMIN")

                        // Authenticate any other request not explicitly permitted above
                        .anyRequest().authenticated()
                )

                // Use the standard filter placement
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Maintain the security context settings you provided
                .securityContext((securityContext) -> securityContext.requireExplicitSave(false))
                .requestCache((requestCache) -> requestCache.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}