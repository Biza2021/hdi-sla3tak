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
        this.userId = user.getId();
        this.shopId = user.getShop().getId();
        this.shopSlug = user.getShop().getSlug();
        this.fullName = user.getFullName();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.active = user.isActive();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
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
