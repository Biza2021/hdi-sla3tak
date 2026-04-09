package com.hdisla3tak.app.web;

import com.hdisla3tak.app.domain.AppUser;
import com.hdisla3tak.app.domain.Shop;
import com.hdisla3tak.app.domain.enums.UserRole;
import com.hdisla3tak.app.repository.AppUserRepository;
import com.hdisla3tak.app.service.ShopService;
import com.hdisla3tak.app.web.form.SetupOwnerForm;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Controller
public class SetupController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShopService shopService;
    private final MessageSource messageSource;

    public SetupController(AppUserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           ShopService shopService,
                           MessageSource messageSource) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.shopService = shopService;
        this.messageSource = messageSource;
    }

    @GetMapping("/setup")
    public String legacyFormRedirect() {
        return "redirect:/shops/new";
    }

    @GetMapping("/shops/new")
    public String form(Model model) {
        if (!model.containsAttribute("setupOwnerForm")) {
            model.addAttribute("setupOwnerForm", new SetupOwnerForm());
        }
        return "setup";
    }

    @PostMapping({"/setup", "/shops/new"})
    @Transactional
    public String submit(@Valid @ModelAttribute("setupOwnerForm") SetupOwnerForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Locale locale) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "confirmPassword", messageSource.getMessage("validation.setup.passwordsMismatch", null, locale));
        }
        String normalizedShopSlug = shopService.normalizeShopSlug(form.getShopSlug());
        form.setShopSlug(normalizedShopSlug);
        if (!shopService.isValidShopSlug(normalizedShopSlug)) {
            bindingResult.rejectValue("shopSlug", "shopSlug", messageSource.getMessage("validation.shop.slug.invalid", null, locale));
        } else if (shopService.existsBySlug(normalizedShopSlug)) {
            bindingResult.rejectValue("shopSlug", "shopSlug", messageSource.getMessage("validation.shop.slug.exists", null, locale));
        }
        if (bindingResult.hasErrors()) {
            return "setup";
        }

        String username = normalizeUsername(form.getUsername());

        try {
            Shop shop = shopService.createShop(form.getBusinessName(), normalizedShopSlug);
            AppUser owner = new AppUser();
            owner.setFullName(form.getFullName().trim());
            owner.setUsername(username);
            owner.setShop(shop);
            owner.setPasswordHash(passwordEncoder.encode(form.getPassword()));
            owner.setRole(UserRole.ADMIN);
            owner.setActive(true);
            userRepository.save(owner);

            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("flash.setup.complete", null, locale));
            return "redirect:/" + shop.getSlug() + "/login";
        } catch (IllegalArgumentException ex) {
            if ("duplicate-shop-slug".equals(ex.getMessage())) {
                bindingResult.rejectValue("shopSlug", "shopSlug", messageSource.getMessage("validation.shop.slug.exists", null, locale));
                return "setup";
            }
            if ("invalid-shop-slug".equals(ex.getMessage())) {
                bindingResult.rejectValue("shopSlug", "shopSlug", messageSource.getMessage("validation.shop.slug.invalid", null, locale));
                return "setup";
            }
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            if (shopService.existsBySlug(normalizedShopSlug)) {
                bindingResult.rejectValue("shopSlug", "shopSlug", messageSource.getMessage("validation.shop.slug.exists", null, locale));
            } else {
                bindingResult.rejectValue("username", "username", messageSource.getMessage("validation.user.username.exists", null, locale));
            }
            return "setup";
        }
    }

    private String normalizeUsername(String username) {
        return StringUtils.hasText(username) ? username.trim() : null;
    }
}
