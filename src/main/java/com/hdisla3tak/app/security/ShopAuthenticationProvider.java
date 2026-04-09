package com.hdisla3tak.app.security;

import com.hdisla3tak.app.service.AppUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ShopAuthenticationProvider implements AuthenticationProvider {

    private final AppUserDetailsService appUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public ShopAuthenticationProvider(AppUserDetailsService appUserDetailsService,
                                      PasswordEncoder passwordEncoder) {
        this.appUserDetailsService = appUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName() == null ? null : authentication.getName().trim();
        String password = authentication.getCredentials() == null ? "" : authentication.getCredentials().toString();
        String shopSlug = extractShopSlug(authentication);

        if (!StringUtils.hasText(username) || !StringUtils.hasText(shopSlug)) {
            throw new BadCredentialsException("Invalid login request.");
        }

        ShopUserPrincipal principal = appUserDetailsService.loadUserByUsernameAndShopSlug(username, shopSlug)
            .orElseThrow(() -> new BadCredentialsException("Invalid username or password."));

        if (!principal.isEnabled()) {
            throw new DisabledException("User account is inactive.");
        }
        if (!passwordEncoder.matches(password, principal.getPassword())) {
            throw new BadCredentialsException("Invalid username or password.");
        }

        UsernamePasswordAuthenticationToken authenticated = UsernamePasswordAuthenticationToken.authenticated(
            principal,
            null,
            principal.getAuthorities()
        );
        authenticated.setDetails(authentication.getDetails());
        return authenticated;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private String extractShopSlug(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof ShopAuthenticationDetails shopAuthenticationDetails) {
            return shopAuthenticationDetails.getShopSlug();
        }
        return null;
    }
}
