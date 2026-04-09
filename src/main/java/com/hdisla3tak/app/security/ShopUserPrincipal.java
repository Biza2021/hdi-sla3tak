package com.hdisla3tak.app.security;

import com.hdisla3tak.app.domain.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class ShopUserPrincipal implements UserDetails {

    private final Long userId;
    private final Long shopId;
    private final String shopSlug;
    private final String fullName;
    private final String username;
    private final String passwordHash;
    private final boolean active;
    private final List<GrantedAuthority> authorities;

    public ShopUserPrincipal(AppUser user) {
        this(
            user.getId(),
            user.getShop().getId(),
            user.getShop().getSlug(),
            user.getFullName(),
            user.getUsername(),
            user.getPasswordHash(),
            user.isActive(),
            user.getRole().name()
        );
    }

    public ShopUserPrincipal(Long userId,
                             Long shopId,
                             String shopSlug,
                             String fullName,
                             String username,
                             String passwordHash,
                             boolean active,
                             String roleName) {
        this.userId = userId;
        this.shopId = shopId;
        this.shopSlug = shopSlug;
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.active = active;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    public Long getUserId() {
        return userId;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getShopSlug() {
        return shopSlug;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
