package com.hdisla3tak.app.web;

import com.hdisla3tak.app.repository.AppUserRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@Controller
public class AuthController {

    private final AppUserRepository userRepository;
    private final MessageSource messageSource;

    public AuthController(AppUserRepository userRepository, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) Boolean error,
                        @RequestParam(value = "logout", required = false) Boolean logout,
                        Model model,
                        Locale locale) {
        if (userRepository.count() == 0) {
            return "redirect:/setup";
        }
        if (Boolean.TRUE.equals(error)) {
            model.addAttribute("errorMessage", messageSource.getMessage("flash.auth.invalidLogin", null, locale));
        }
        if (Boolean.TRUE.equals(logout)) {
            model.addAttribute("successMessage", messageSource.getMessage("flash.auth.loggedOut", null, locale));
        }
        return "login";
    }
}
