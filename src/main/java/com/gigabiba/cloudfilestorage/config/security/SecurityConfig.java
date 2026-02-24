package com.gigabiba.cloudfilestorage.config.security;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.gigabiba.cloudfilestorage.config.security.authentication.RestAuthenticatedEntryPoint;
import com.gigabiba.cloudfilestorage.config.security.authentication.jsonAuthentication.JsonAuthFilter;
import com.gigabiba.cloudfilestorage.config.security.authentication.jsonAuthentication.JsonAuthSuccessHandler;
import com.gigabiba.cloudfilestorage.models.Role;
import com.gigabiba.cloudfilestorage.repository.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;

import static org.springframework.http.HttpMethod.OPTIONS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${spring.url.NGINX_URL}")
    private String NGINX_URL;

    private final UserRepository userRepository;

    @Autowired
    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



    @Bean
    public org.springframework.security.web.SecurityFilterChain SecurityFilterChain(HttpSecurity http,
                                                                                    AuthenticationManager authenticationManager,
                                                                                    SecurityContextRepository securityContextRepository,
                                                                                    JsonAuthSuccessHandler jsonAuthSuccessHandler,
                                                                                    RestAuthenticatedEntryPoint entryPoint,
                                                                                    ObjectMapper objectMapper)
            throws Exception {

        JsonAuthFilter filter = new JsonAuthFilter(objectMapper, authenticationManager);
        filter.setAuthenticationSuccessHandler(jsonAuthSuccessHandler);

        http.cors(Customizer.withDefaults());

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityContext(securityContext ->
                        securityContext.securityContextRepository(securityContextRepository)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(OPTIONS,
                                "/api/auth/me",
                                "/api/resource",
                                "/api/resource/download",
                                "/api/resource/search",
                                "/api/resource/move",
                                "/api/directory",
                                "/api/resource",
                                "/api/directory").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/sign-in",
                                "/api/auth/sign-up",
                                "/api/auth/sign-out").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/auth/me").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/resource",
                                "/api/resource/download",
                                "/api/resource/search",
                                "/api/resource/move",
                                "/api/directory").authenticated()
                        .requestMatchers(HttpMethod.POST,
                                "/api/resource",
                                "/api/directory").authenticated()
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/resource").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                )
                .addFilterAt(filter, UsernamePasswordAuthenticationFilter.class)
                .anonymous(Customizer.withDefaults())
                .requestCache(RequestCacheConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/sign-out")
                        .invalidateHttpSession(true)
                        .deleteCookies("SESSION")
                        .clearAuthentication(true)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            if (authentication == null) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            } else {
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                            }
                        }))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(NGINX_URL));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT,
                HttpHeaders.CONTENT_DISPOSITION, "X-Requested-With", HttpHeaders.AUTHORIZATION));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    @Bean
    @Primary
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean //for autologin
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }

    @Bean
    @Primary
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(5);
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
                Optional<com.gigabiba.cloudfilestorage.models.User> user = userRepository.findByUsernameIgnoreCase(login);
                return user.map(u -> new org.springframework.security.core.userdetails.User(
                                u.getUsername(),
                                u.getPassword(),
                                Collections.singleton(Role.valueOf(u.getRole()))
                        ))
                        .orElseThrow(() -> new UsernameNotFoundException(login + " user not found"));
            }
        };
    }
}
