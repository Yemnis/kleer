package com.kleer.currency.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Web configuration for CORS and other web-related settings.
 * 
 * Enables CORS to allow the React frontend to communicate with the backend.
 * Configures H2 console access by allowing frames for the console path.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:5174")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Filter to allow H2 console to be displayed in frames.
     * This is necessary for the H2 console UI to work properly.
     * 
     * WARNING: Only use this in development environments!
     */
    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> h2ConsoleFrameOptionsFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, 
                                          HttpServletResponse response, 
                                          FilterChain filterChain) throws ServletException, IOException {
                String requestURI = request.getRequestURI();
                if (requestURI.startsWith("/h2-console")) {
                    // Allow frames for H2 console
                    response.setHeader("X-Frame-Options", "SAMEORIGIN");
                    // Disable Content Security Policy for H2 console
                    response.setHeader("Content-Security-Policy", "frame-ancestors 'self'");
                }
                filterChain.doFilter(request, response);
            }
        });
        
        registrationBean.setOrder(1);
        registrationBean.addUrlPatterns("/h2-console/*");
        return registrationBean;
    }
}

