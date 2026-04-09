package com.hdisla3tak.app.tenant;

import com.hdisla3tak.app.domain.Shop;
import com.hdisla3tak.app.security.ShopUserPrincipal;
import com.hdisla3tak.app.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
public class ShopContextInterceptor implements HandlerInterceptor {

    private final ShopService shopService;
    private final ShopContext shopContext;

    public ShopContextInterceptor(ShopService shopService, ShopContext shopContext) {
        this.shopService = shopService;
        this.shopContext = shopContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        shopContext.clear();

        @SuppressWarnings("unchecked")
        Map<String, String> uriVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (uriVariables == null) {
            return true;
        }

        String shopSlug = uriVariables.get("shopSlug");
        if (shopSlug == null || shopSlug.isBlank()) {
            return true;
        }

        Shop shop = shopService.findBySlug(shopSlug)
            .orElse(null);
        if (shop == null) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof ShopUserPrincipal shopUserPrincipal)) {
                response.sendError(HttpStatus.FORBIDDEN.value());
                return false;
            }
            if (!shop.getId().equals(shopUserPrincipal.getShopId())) {
                response.sendError(HttpStatus.NOT_FOUND.value());
                return false;
            }
        }

        shopContext.setCurrentShop(shop);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        shopContext.clear();
    }
}
