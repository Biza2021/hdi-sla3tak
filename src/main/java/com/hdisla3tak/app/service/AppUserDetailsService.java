package com.hdisla3tak.app.service;

import com.hdisla3tak.app.repository.AppUserRepository;
import com.hdisla3tak.app.security.AuthenticatedShopUser;
import com.hdisla3tak.app.security.ShopUserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class AppUserDetailsService {

    private final AppUserRepository userRepository;

    public AppUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ShopUserPrincipal> loadUserByUsernameAndShopSlug(String username, String shopSlug) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(shopSlug)) {
            return Optional.empty();
        }
        return userRepository.findAuthenticatedUserByUsernameAndShopSlug(username.trim(), shopSlug.trim())
            .map(this::toPrincipal);
    }

    public ShopUserPrincipal toPrincipal(AuthenticatedShopUser user) {
        return new ShopUserPrincipal(
            user.userId(),
            user.shopId(),
            user.shopSlug(),
            user.fullName(),
            user.username(),
            user.passwordHash(),
            user.active(),
            user.role().name()
        );
    }
}
