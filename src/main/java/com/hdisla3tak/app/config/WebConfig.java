package com.hdisla3tak.app.config;

import com.hdisla3tak.app.tenant.ShopContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ShopContextInterceptor shopContextInterceptor;

    public WebConfig(ShopContextInterceptor shopContextInterceptor) {
        this.shopContextInterceptor = shopContextInterceptor;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/error/404").setViewName("error/404");
        registry.addViewController("/error/500").setViewName("error/500");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(shopContextInterceptor)
            .addPathPatterns("/*", "/*/login", "/*/admin/**", "/*/customers/**", "/*/items/**")
            .excludePathPatterns("/shops/**", "/setup", "/setup/**", "/login", "/track/**", "/healthz", "/error/**", "/css/**", "/js/**", "/img/**", "/h2-console/**");
    }
}
