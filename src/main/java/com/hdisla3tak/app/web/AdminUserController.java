package com.hdisla3tak.app.web;

import com.hdisla3tak.app.domain.AppUser;
import com.hdisla3tak.app.domain.Shop;
import com.hdisla3tak.app.domain.enums.UserRole;
import com.hdisla3tak.app.repository.AppUserRepository;
import com.hdisla3tak.app.security.ShopUserPrincipal;
import com.hdisla3tak.app.service.ShopService;
import com.hdisla3tak.app.service.ShopSettingsService;
import com.hdisla3tak.app.tenant.ShopContext;
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
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Locale;

@Controller
@RequestMapping("/{shopSlug}/admin/users")
public class AdminUserController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShopService shopService;
    private final ShopSettingsService shopSettingsService;
    private final MessageSource messageSource;
    private final ShopContext shopContext;

    public AdminUserController(AppUserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               ShopService shopService,
                               ShopSettingsService shopSettingsService,
                               MessageSource messageSource,
                               ShopContext shopContext) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.shopService = shopService;
        this.shopSettingsService = shopSettingsService;
        this.messageSource = messageSource;
        this.shopContext = shopContext;
    }

    @GetMapping
    public String list(@PathVariable String shopSlug, Model model) {
        populateListModel(model);
        if (!model.containsAttribute("businessNameForm")) {
            BusinessNameForm form = new BusinessNameForm();
            form.setBusinessName(shopSettingsService.getEditableBusinessName());
            model.addAttribute("businessNameForm", form);
        }
        model.addAttribute("shopSlug", shopSlug);
        return "admin/users";
    }

    @PostMapping("/business-name")
    public String updateBusinessName(@PathVariable String shopSlug,
                                     @Valid @ModelAttribute("businessNameForm") BusinessNameForm businessNameForm,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Model model,
                                     Locale locale) {
        if (bindingResult.hasErrors()) {
            populateListModel(model);
            model.addAttribute("shopSlug", shopSlug);
            return "admin/users";
        }
        Shop currentShop = currentShop();
        shopService.updateShopName(currentShop, businessNameForm.getBusinessName());
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.settings.businessNameUpdated", null, locale));
        return "redirect:/" + shopSlug + "/admin/users";
    }

    @GetMapping("/new")
    public String createForm(@PathVariable String shopSlug, Model model) {
        model.addAttribute("pageTitleKey", "users.create.title");
        model.addAttribute("pageSubtitleKey", "users.create.subtitle");
        model.addAttribute("userForm", new UserForm());
        model.addAttribute("editing", false);
        model.addAttribute("shopSlug", shopSlug);
        return "admin/user-form";
    }

    @PostMapping
    public String create(@PathVariable String shopSlug,
                         @Valid @ModelAttribute("userForm") UserForm userForm,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model,
                         Locale locale) {
        String username = normalizeUsername(userForm.getUsername());
        if (username != null && userRepository.existsByUsernameIgnoreCaseAndShop_Id(username, currentShop().getId())) {
            bindingResult.rejectValue("username", "username", messageSource.getMessage("validation.user.username.exists", null, locale));
        }
        if (userForm.getPassword() == null || userForm.getPassword().trim().length() < 6) {
            bindingResult.rejectValue("password", "password", messageSource.getMessage("validation.user.password.create", null, locale));
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitleKey", "users.create.title");
            model.addAttribute("pageSubtitleKey", "users.create.subtitle");
            model.addAttribute("editing", false);
            model.addAttribute("shopSlug", shopSlug);
            return "admin/user-form";
        }
        AppUser user = new AppUser();
        user.setFullName(userForm.getFullName().trim());
        user.setUsername(username);
        user.setShop(currentShop());
        user.setPasswordHash(passwordEncoder.encode(userForm.getPassword()));
        user.setRole(userForm.getRole());
        user.setActive(userForm.isActive());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.user.created", null, locale));
        return "redirect:/" + shopSlug + "/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String shopSlug, @PathVariable Long id, Model model) {
        AppUser user = getUserById(id);
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
        model.addAttribute("shopSlug", shopSlug);
        return "admin/user-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable String shopSlug,
                         @PathVariable Long id,
                         @Valid @ModelAttribute("userForm") UserForm userForm,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model,
                         Locale locale) {
        AppUser user = getUserById(id);
        String username = normalizeUsername(userForm.getUsername());
        if (username != null && userRepository.findByUsernameIgnoreCaseAndShop_Id(username, currentShop().getId())
            .filter(existing -> !existing.getId().equals(id)).isPresent()) {
            bindingResult.rejectValue("username", "username", messageSource.getMessage("validation.user.username.exists", null, locale));
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitleKey", "users.edit.title");
            model.addAttribute("pageSubtitleKey", "users.edit.subtitle");
            model.addAttribute("user", user);
            model.addAttribute("editing", true);
            model.addAttribute("shopSlug", shopSlug);
            return "admin/user-form";
        }
        user.setFullName(userForm.getFullName().trim());
        user.setUsername(username);
        if (user.getShop() == null) {
            user.setShop(currentShop());
        }
        user.setRole(userForm.getRole());
        user.setActive(userForm.isActive());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.user.updated", null, locale));
        return "redirect:/" + shopSlug + "/admin/users";
    }

    @GetMapping("/{id}/password")
    public String passwordForm(@PathVariable String shopSlug, @PathVariable Long id, Model model) {
        AppUser user = getUserById(id);
        model.addAttribute("pageTitleKey", "users.password.title");
        model.addAttribute("pageSubtitleKey", "users.password.subtitle");
        model.addAttribute("user", user);
        model.addAttribute("userPasswordForm", new UserPasswordForm());
        model.addAttribute("shopSlug", shopSlug);
        return "admin/user-password";
    }

    @PostMapping("/{id}/password")
    public String updatePassword(@PathVariable String shopSlug,
                                 @PathVariable Long id,
                                 @Valid @ModelAttribute("userPasswordForm") UserPasswordForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 Locale locale) {
        AppUser user = getUserById(id);
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "confirmPassword", messageSource.getMessage("validation.setup.passwordsMismatch", null, locale));
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitleKey", "users.password.title");
            model.addAttribute("pageSubtitleKey", "users.password.subtitle");
            model.addAttribute("user", user);
            model.addAttribute("shopSlug", shopSlug);
            return "admin/user-password";
        }
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.user.passwordChanged", null, locale));
        return "redirect:/" + shopSlug + "/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String shopSlug,
                         @PathVariable Long id,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes,
                         Locale locale) {
        AppUser user = getUserById(id);
        if (authentication != null
            && authentication.getPrincipal() instanceof ShopUserPrincipal principal
            && principal.getUserId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.user.cannotDeleteSelf", null, locale));
            return "redirect:/" + shopSlug + "/admin/users";
        }
        long adminCount = userRepository.countByRoleAndShop_Id(UserRole.ADMIN, currentShop().getId());
        if (adminCount <= 1 && user.getRole() == UserRole.ADMIN) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.user.lastAdmin", null, locale));
            return "redirect:/" + shopSlug + "/admin/users";
        }
        userRepository.delete(user);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.user.deleted", null, locale));
        return "redirect:/" + shopSlug + "/admin/users";
    }

    private void populateListModel(Model model) {
        model.addAttribute("pageTitleKey", "users.title");
        model.addAttribute("pageSubtitleKey", "users.subtitle");
        model.addAttribute("users", userRepository.findAllByShop_IdOrderByCreatedAtDesc(currentShop().getId()));
    }

    private String normalizeUsername(String username) {
        return StringUtils.hasText(username) ? username.trim() : null;
    }

    private Shop currentShop() {
        return shopContext.requireCurrentShop();
    }

    private AppUser getUserById(Long id) {
        return userRepository.findByIdAndShop_Id(id, currentShop().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
