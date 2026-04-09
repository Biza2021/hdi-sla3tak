package com.hdisla3tak.app.config;

import com.hdisla3tak.app.security.ShopAuthenticationDetails;
import com.hdisla3tak.app.security.ShopAuthenticationProvider;
import com.hdisla3tak.app.security.ShopUserPrincipal;
import com.hdisla3tak.app.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.StringUtils;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ShopAuthenticationProvider shopAuthenticationProvider,
                                                   AuthenticationSuccessHandler authenticationSuccessHandler,
                                                   AuthenticationFailureHandler authenticationFailureHandler,
                                                   AuthenticationEntryPoint authenticationEntryPoint,
                                                   LogoutSuccessHandler logoutSuccessHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**", "/login", "/setup", "/setup/**", "/shops/**", "/track/**", "/healthz", "/error/**", "/h2-console/**", "/*/login").permitAll()
                .requestMatchers("/*/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(authenticationEntryPoint))
            .authenticationProvider(shopAuthenticationProvider)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .authenticationDetailsSource(ShopAuthenticationDetails::new)
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessHandler(logoutSuccessHandler)
                .permitAll())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/shops/**");
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> response.sendRedirect(resolveShopHome(authentication));
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String shopSlug = trimToNull(request.getParameter("shopSlug"));
            if (shopSlug != null) {
                response.sendRedirect("/" + shopSlug + "/login?error=true");
                return;
            }
            response.sendRedirect("/login?error=true");
        };
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            String shopSlug = trimToNull(request.getParameter("shopSlug"));
            if (shopSlug == null && authentication != null && authentication.getPrincipal() instanceof ShopUserPrincipal principal) {
                shopSlug = principal.getShopSlug();
            }
            if (shopSlug != null) {
                response.sendRedirect("/" + shopSlug + "/login?logout=true");
                return;
            }
            response.sendRedirect("/login?logout=true");
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ShopService shopService) {
        return (request, response, authException) -> {
            if (request.getRequestURI().equals("/")) {
                response.sendRedirect("/login");
                return;
            }

            String shopSlug = extractLeadingPathSegment(request);
            if (shopSlug != null && shopService.findBySlug(shopSlug).isPresent()) {
                response.sendRedirect("/" + shopSlug + "/login");
                return;
            }

            response.sendRedirect("/login");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private String resolveShopHome(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof ShopUserPrincipal principal) {
            return "/" + principal.getShopSlug();
        }
        return "/";
    }

    private String extractLeadingPathSegment(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!StringUtils.hasText(uri) || "/".equals(uri)) {
            return null;
        }
        String trimmed = uri.startsWith("/") ? uri.substring(1) : uri;
        int slashIndex = trimmed.indexOf('/');
        return slashIndex >= 0 ? trimmed.substring(0, slashIndex) : trimmed;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
