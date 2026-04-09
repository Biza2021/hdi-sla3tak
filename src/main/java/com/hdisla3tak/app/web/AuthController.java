package com.hdisla3tak.app.web;

import com.hdisla3tak.app.security.ShopUserPrincipal;
import com.hdisla3tak.app.service.ShopService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Controller
public class AuthController {

    private final ShopService shopService;
    private final MessageSource messageSource;

    public AuthController(ShopService shopService,
                          MessageSource messageSource) {
        this.shopService = shopService;
        this.messageSource = messageSource;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) Boolean error,
                        @RequestParam(value = "logout", required = false) Boolean logout,
                        Model model,
                        Locale locale,
                        Authentication authentication) {
        if (!shopService.hasAnyShops()) {
            return "redirect:/shops/new";
        }
        if (isAuthenticated(authentication) && authentication.getPrincipal() instanceof ShopUserPrincipal principal) {
            return "redirect:/" + principal.getShopSlug();
        }
        return shopService.findSingleShop()
            .map(shop -> "redirect:/" + shop.getSlug() + "/login" + buildLoginQuery(error, logout))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{shopSlug}/login")
    public String shopLogin(@PathVariable String shopSlug,
                            @RequestParam(value = "error", required = false) Boolean error,
                            @RequestParam(value = "logout", required = false) Boolean logout,
                            Model model,
                            Locale locale,
                            Authentication authentication) {
        if (!shopService.hasAnyShops()) {
            return "redirect:/shops/new";
        }
        if (isAuthenticated(authentication) && authentication.getPrincipal() instanceof ShopUserPrincipal principal) {
            return "redirect:/" + principal.getShopSlug();
        }
        if (Boolean.TRUE.equals(error)) {
            model.addAttribute("errorMessage", messageSource.getMessage("flash.auth.invalidLogin", null, locale));
        }
        if (Boolean.TRUE.equals(logout)) {
            model.addAttribute("successMessage", messageSource.getMessage("flash.auth.loggedOut", null, locale));
        }
        model.addAttribute("shopSlug", shopSlug);
        return "login";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private String buildLoginQuery(Boolean error, Boolean logout) {
        if (Boolean.TRUE.equals(error)) {
            return "?error=true";
        }
        if (Boolean.TRUE.equals(logout)) {
            return "?logout=true";
        }
        return "";
    }
}
