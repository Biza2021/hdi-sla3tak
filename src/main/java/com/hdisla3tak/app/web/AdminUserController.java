package com.hdisla3tak.app.web;

import com.hdisla3tak.app.domain.AppUser;
import com.hdisla3tak.app.repository.AppUserRepository;
import com.hdisla3tak.app.service.ShopSettingsService;
import com.hdisla3tak.app.web.form.BusinessNameForm;
import com.hdisla3tak.app.web.form.UserForm;
import com.hdisla3tak.app.web.form.UserPasswordForm;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShopSettingsService shopSettingsService;
    private final MessageSource messageSource;

    public AdminUserController(AppUserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               ShopSettingsService shopSettingsService,
                               MessageSource messageSource) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.shopSettingsService = shopSettingsService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String list(Model model) {
        populateListModel(model);
        if (!model.containsAttribute("businessNameForm")) {
            BusinessNameForm form = new BusinessNameForm();
            form.setBusinessName(shopSettingsService.getEditableBusinessName());
            model.addAttribute("businessNameForm", form);
        }
        return "admin/users";
    }

    @PostMapping("/business-name")
    public String updateBusinessName(@Valid @ModelAttribute("businessNameForm") BusinessNameForm businessNameForm,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Model model,
                                     Locale locale) {
        if (bindingResult.hasErrors()) {
            populateListModel(model);
            return "admin/users";
        }
        shopSettingsService.saveBusinessName(businessNameForm.getBusinessName());
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.settings.businessNameUpdated", null, locale));
        return "redirect:/admin/users";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitleKey", "users.create.title");
        model.addAttribute("pageSubtitleKey", "users.create.subtitle");
        model.addAttribute("userForm", new UserForm());
        model.addAttribute("editing", false);
        return "admin/user-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("userForm") UserForm userForm,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model,
                         Locale locale) {
        if (userRepository.existsByUsernameIgnoreCase(userForm.getUsername())) {
            bindingResult.rejectValue("username", "username", messageSource.getMessage("validation.user.username.exists", null, locale));
        }
        if (userForm.getPassword() == null || userForm.getPassword().trim().length() < 6) {
            bindingResult.rejectValue("password", "password", messageSource.getMessage("validation.user.password.create", null, locale));
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitleKey", "users.create.title");
            model.addAttribute("pageSubtitleKey", "users.create.subtitle");
            model.addAttribute("editing", false);
            return "admin/user-form";
        }
        AppUser user = new AppUser();
        user.setFullName(userForm.getFullName().trim());
        user.setUsername(userForm.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(userForm.getPassword()));
        user.setRole(userForm.getRole());
        user.setActive(userForm.isActive());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.user.created", null, locale));
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        AppUser user = userRepository.findById(id).orElseThrow();
        UserForm form = new UserForm();
        form.setFullName(user.getFullName());
        form.setUsername(user.getUsername());
        form.setRole(user.getRole());
        form.setActive(user.isActive());
        model.addAttribute("pageTitleKey", "users.edit.title");
        model.addAttribute("pageSubtitleKey", "users.edit.subtitle");
        model.addAttribute("user", user);
        model.addAttribute("userForm", form);
        model.addAttribute("editing", true);
        return "admin/user-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("userForm") UserForm userForm,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model,
                         Locale locale) {
        AppUser user = userRepository.findById(id).orElseThrow();
        if (userRepository.findByUsernameIgnoreCase(userForm.getUsername())
            .filter(existing -> !existing.getId().equals(id)).isPresent()) {
            bindingResult.rejectValue("username", "username", messageSource.getMessage("validation.user.username.exists", null, locale));
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitleKey", "users.edit.title");
            model.addAttribute("pageSubtitleKey", "users.edit.subtitle");
            model.addAttribute("user", user);
            model.addAttribute("editing", true);
            return "admin/user-form";
        }
        user.setFullName(userForm.getFullName().trim());
        user.setUsername(userForm.getUsername().trim());
        user.setRole(userForm.getRole());
        user.setActive(userForm.isActive());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.user.updated", null, locale));
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/password")
    public String passwordForm(@PathVariable Long id, Model model) {
        AppUser user = userRepository.findById(id).orElseThrow();
        model.addAttribute("pageTitleKey", "users.password.title");
        model.addAttribute("pageSubtitleKey", "users.password.subtitle");
        model.addAttribute("user", user);
        model.addAttribute("userPasswordForm", new UserPasswordForm());
        return "admin/user-password";
    }

    @PostMapping("/{id}/password")
    public String updatePassword(@PathVariable Long id,
                                 @Valid @ModelAttribute("userPasswordForm") UserPasswordForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 Locale locale) {
        AppUser user = userRepository.findById(id).orElseThrow();
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "confirmPassword", messageSource.getMessage("validation.setup.passwordsMismatch", null, locale));
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitleKey", "users.password.title");
            model.addAttribute("pageSubtitleKey", "users.password.subtitle");
            model.addAttribute("user", user);
            return "admin/user-password";
        }
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.user.passwordChanged", null, locale));
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes,
                         Locale locale) {
        AppUser user = userRepository.findById(id).orElseThrow();
        if (authentication != null && authentication.getName().equalsIgnoreCase(user.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.user.cannotDeleteSelf", null, locale));
            return "redirect:/admin/users";
        }
        long adminCount = userRepository.findAll().stream().filter(u -> u.getRole().name().equals("ADMIN")).count();
        if (adminCount <= 1 && user.getRole().name().equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.user.lastAdmin", null, locale));
            return "redirect:/admin/users";
        }
        userRepository.delete(user);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.user.deleted", null, locale));
        return "redirect:/admin/users";
    }

    private void populateListModel(Model model) {
        model.addAttribute("pageTitleKey", "users.title");
        model.addAttribute("pageSubtitleKey", "users.subtitle");
        model.addAttribute("users", userRepository.findAll().stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .toList());
    }
}
