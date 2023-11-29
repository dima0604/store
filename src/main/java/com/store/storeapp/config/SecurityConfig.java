package com.store.storeapp.config;

import com.store.storeapp.controllers.StoreController;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Configuration
@EnableWebSecurity
//@EnableMethodSecurity
public class SecurityConfig {
    @Value("${login}")
    private List<String> login;
    @Value("${pass}")
    private List<String> pass;

    private static final Logger LOGGER = LogManager.getLogger(StoreController.class);


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authConfig -> {
                    authConfig
                            .requestMatchers("/js/**").permitAll()
                            .requestMatchers("/confirmTest").permitAll()
                            .anyRequest().authenticated();
                })
                .exceptionHandling(exception ->
                        exception.accessDeniedPage("/unauthorized"))
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login")
                                .successHandler(authenticationSuccessHandler())
                                .loginProcessingUrl("/store_spring_security_check")
                                .failureUrl("/login?error")
                                .usernameParameter("store_login")
                                .passwordParameter("store_password")
                                .permitAll())

                .logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout")
                                .permitAll()
                )
                .sessionManagement(management->
                        management
                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                .invalidSessionUrl("/login")
                                .maximumSessions(1)
                                .maxSessionsPreventsLogin(false)
                                .expiredUrl("/login")
                        );
        return http.build();
    }
    @Bean
    public UserDetailsService userDetailsService() {
        List<UserDetails> users = new ArrayList<>();
        for (int i = 0; i < login.size(); i++) {
            UserDetails user =
                    User.builder()
                            .username(login.get(i))
                            .password(passwordEncoder().encode(pass.get(i)))
                            .roles("USER")
                            .build();
            users.add(user);
        }
        return new InMemoryUserDetailsManager(users);
    }
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }
    private static class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) throws IOException, ServletException {

            LOGGER.info("User " + authentication.getName() + " successfully logged, IP: " + getClientIpAddress(request));
            response.sendRedirect("/");
        }

    }
    private static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        }
        return xForwardedForHeader.split(",")[0];
    }

}
