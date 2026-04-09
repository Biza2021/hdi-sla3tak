package com.hdisla3tak.app.service;

import com.hdisla3tak.app.domain.AppUser;
import com.hdisla3tak.app.security.ShopUserPrincipal;
import com.hdisla3tak.app.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class AppUserDetailsService {

    private final AppUserRepository userRepository;

    public AppUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<ShopUserPrincipal> loadUserByUsernameAndShopSlug(String username, String shopSlug) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(shopSlug)) {
            return Optional.empty();
        }
        return userRepository.findByUsernameIgnoreCaseAndShop_SlugIgnoreCase(username.trim(), shopSlug.trim())
            .map(this::toPrincipal);
    }

    public ShopUserPrincipal toPrincipal(AppUser user) {
        return new ShopUserPrincipal(user);
    }
}
