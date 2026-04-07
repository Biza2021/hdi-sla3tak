package com.hdisla3tak.app.web;

import com.hdisla3tak.app.domain.AppUser;
import com.hdisla3tak.app.domain.enums.UserRole;
import com.hdisla3tak.app.repository.AppUserRepository;
import com.hdisla3tak.app.service.ShopSettingsService;
import com.hdisla3tak.app.web.form.SetupOwnerForm;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
public class SetupController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShopSettingsService shopSettingsService;
    private final MessageSource messageSource;

    public SetupController(AppUserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           ShopSettingsService shopSettingsService,
                           MessageSource messageSource) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.shopSettingsService = shopSettingsService;
        this.messageSource = messageSource;
    }

    @GetMapping("/setup")
    public String form(Model model) {
        if (userRepository.count() > 0) {
            return "redirect:/login";
        }
        model.addAttribute("setupOwnerForm", new SetupOwnerForm());
        return "setup";
    }

    @PostMapping("/setup")
    public String submit(@Valid @ModelAttribute("setupOwnerForm") SetupOwnerForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Locale locale) {
        if (userRepository.count() > 0) {
            return "redirect:/login";
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "confirmPassword", messageSource.getMessage("validation.setup.passwordsMismatch", null, locale));
        }
        if (userRepository.existsByUsernameIgnoreCase(form.getUsername())) {
            bindingResult.rejectValue("username", "username", messageSource.getMessage("validation.user.username.exists", null, locale));
        }
        if (bindingResult.hasErrors()) {
            return "setup";
        }

        AppUser owner = new AppUser();
        owner.setFullName(form.getFullName().trim());
        owner.setUsername(form.getUsername().trim());
        owner.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        owner.setRole(UserRole.ADMIN);
        owner.setActive(true);
        userRepository.save(owner);
        shopSettingsService.saveBusinessName(form.getBusinessName());

        redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("flash.setup.complete", null, locale));
        return "redirect:/login";
    }
}
