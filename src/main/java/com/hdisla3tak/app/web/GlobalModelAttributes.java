package com.hdisla3tak.app.web;

import com.hdisla3tak.app.domain.enums.ItemCategory;
import com.hdisla3tak.app.domain.enums.RepairStatus;
import com.hdisla3tak.app.domain.enums.UserRole;
import com.hdisla3tak.app.repository.AppUserRepository;
import com.hdisla3tak.app.service.ShopSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAttributes {

    private final AppUserRepository userRepository;
    private final ShopSettingsService shopSettingsService;

    public GlobalModelAttributes(AppUserRepository userRepository,
                                 ShopSettingsService shopSettingsService) {
        this.userRepository = userRepository;
        this.shopSettingsService = shopSettingsService;
    }

    @ModelAttribute("repairStatuses")
    public RepairStatus[] repairStatuses() {
        return RepairStatus.values();
    }

    @ModelAttribute("itemCategories")
    public ItemCategory[] itemCategories() {
        return ItemCategory.values();
    }

    @ModelAttribute("userRoles")
    public UserRole[] userRoles() {
        return UserRole.values();
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("setupRequired")
    public boolean setupRequired() {
        return userRepository.count() == 0;
    }

    @ModelAttribute("appDisplayName")
    public String appDisplayName() {
        return shopSettingsService.getBusinessName();
    }
}
