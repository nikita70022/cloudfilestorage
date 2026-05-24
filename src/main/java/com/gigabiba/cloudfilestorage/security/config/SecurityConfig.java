package com.gigabiba.cloudfilestorage.security.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigabiba.cloudfilestorage.config.properties.FrontendProperties;
import com.gigabiba.cloudfilestorage.repository.UserRepository;
import com.gigabiba.cloudfilestorage.security.authentication.JsonAuthFilter;
import com.gigabiba.cloudfilestorage.security.handler.JsonAuthSuccessHandler;
import com.gigabiba.cloudfilestorage.security.handler.RestAuthenticatedEntryPoint;
import com.gigabiba.cloudfilestorage.security.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.http.HttpMethod.OPTIONS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String nginx;

    public SecurityConfig(FrontendProperties props) {
        this.nginx = props.url();
    }


    @Bean
    @Primary
    public JsonAuthFilter jsonAuthFilter(ObjectMapper objectMapper, AuthenticationManager authenticationManager) {
        return new JsonAuthFilter(objectMapper, authenticationManager);
    }


    @Bean
    @Primary
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }


    @Bean
    public UserDetailsServiceImpl userDetailsService(UserRepository userRepository) {

        return new UserDetailsServiceImpl(userRepository);
    }


    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
                                                               BCryptPasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
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
    public org.springframework.security.web.SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JsonAuthFilter jsonAuthFilter,
            RestAuthenticatedEntryPoint entryPoint,
            JsonAuthSuccessHandler jsonAuthSuccessHandler,
            SecurityContextRepository securityContextRepository
    ) throws Exception {

        jsonAuthFilter.setAuthenticationSuccessHandler(jsonAuthSuccessHandler);

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
                .addFilterAt(jsonAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .anonymous(Customizer.withDefaults())
                .requestCache(RequestCacheConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/sign-out")
                        .invalidateHttpSession(true)
                        .deleteCookies("SESSION")
                        .clearAuthentication(true)
                        .logoutSuccessHandler((request, response,
                                               authentication) -> {
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
        config.setAllowedOrigins(List.of(nginx));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT,
                HttpHeaders.CONTENT_DISPOSITION, "X-Requested-With", HttpHeaders.AUTHORIZATION));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
