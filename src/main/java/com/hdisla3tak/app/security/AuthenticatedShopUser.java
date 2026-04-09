package com.hdisla3tak.app.security;

import com.hdisla3tak.app.domain.enums.UserRole;

public record AuthenticatedShopUser(
    Long userId,
    Long shopId,
    String shopSlug,
    String fullName,
    String username,
    String passwordHash,
    boolean active,
    UserRole role
) {
}
