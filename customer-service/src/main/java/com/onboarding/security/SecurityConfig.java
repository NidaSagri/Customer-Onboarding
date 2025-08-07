package com.onboarding.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod; // Make sure this is imported
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security Filter Chain for stateless API endpoints (/api/**).
     * This chain is ordered first and uses HTTP Basic Authentication.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**") // This chain ONLY handles /api/** routes
            .authorizeHttpRequests(authorize -> authorize
                // Allow the public registration API endpoint without any authentication
                .requestMatchers(HttpMethod.POST, "/api/internal/customers/register").permitAll()
                
                // Secure all other internal and admin APIs
                // They now require a user with either ROLE_ADMIN or ROLE_INTERNAL
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "INTERNAL")
                .requestMatchers("/api/internal/**").hasAnyRole("ADMIN", "INTERNAL")
                
                // Any other /api/** routes not matched above will require authentication
                .anyRequest().authenticated()
            )
            // Disable CSRF for stateless APIs that are not called from a browser form
            .csrf(AbstractHttpConfigurer::disable)
            // Tell Spring Security not to create sessions for the API
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Enable HTTP Basic Authentication as the mechanism for API calls
            .httpBasic(withDefaults());

        return http.build();
    }

    /**
     * Security Filter Chain for the stateful UI (all other paths).
     * This chain is ordered second and uses a traditional form-based login.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain uiFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Allow access to static resources like CSS and JS
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher("/css/**"),
                    AntPathRequestMatcher.antMatcher("/js/**")
                ).permitAll()
                // Allow access to the login page and the root path
                .requestMatchers("/", "/login").permitAll()
                // Secure the admin and customer dashboards
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/customer/**", "/dashboard").authenticated()
                // All other un-matched requests must be authenticated
                .anyRequest().authenticated()
            )
            // Configure the form login process
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login") // The URL the form should POST to
                .defaultSuccessUrl("/dashboard", true) // Redirect to dashboard after successful login
                .permitAll() // Allow all users to access the login page
            )
            // Configure the logout process
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST")) // Use POST for logout
                .logoutSuccessUrl("/login?logout") // Redirect to login page with a logout message
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}