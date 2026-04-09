package com.hdisla3tak.app.web;

import com.hdisla3tak.app.security.ShopUserPrincipal;
import com.hdisla3tak.app.service.DashboardService;
import com.hdisla3tak.app.service.ShopService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final ShopService shopService;

    public DashboardController(DashboardService dashboardService,
                               ShopService shopService) {
        this.dashboardService = dashboardService;
        this.shopService = shopService;
    }

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (isAuthenticated(authentication) && authentication.getPrincipal() instanceof ShopUserPrincipal principal) {
            return "redirect:/" + principal.getShopSlug();
        }
        return shopService.findSingleShop()
            .map(shop -> "redirect:/" + shop.getSlug() + "/login")
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{shopSlug}")
    public String dashboard(@PathVariable String shopSlug, Model model, Locale locale) {
        model.addAttribute("pageTitleKey", "dashboard.title");
        model.addAttribute("pageSubtitleKey", "dashboard.subtitle");
        model.addAttribute("stats", dashboardService.getStats(locale));
        model.addAttribute("shopSlug", shopSlug);
        return "dashboard";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
