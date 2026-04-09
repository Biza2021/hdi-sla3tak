package com.hdisla3tak.app.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class ShopAuthenticationDetails extends WebAuthenticationDetails {

    private final String shopSlug;

    public ShopAuthenticationDetails(HttpServletRequest request) {
        super(request);
        String submittedShopSlug = request.getParameter("shopSlug");
        this.shopSlug = submittedShopSlug == null ? null : submittedShopSlug.trim();
    }

    public String getShopSlug() {
        return shopSlug;
    }
}
