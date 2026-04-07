package com.hdisla3tak.app.web;

import com.hdisla3tak.app.domain.Customer;
import com.hdisla3tak.app.service.CustomerService;
import com.hdisla3tak.app.web.form.CustomerForm;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final MessageSource messageSource;

    public CustomerController(CustomerService customerService, MessageSource messageSource) {
        this.customerService = customerService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q, Model model) {
        model.addAttribute("pageTitleKey", "customers.title");
        model.addAttribute("pageSubtitleKey", "customers.subtitle");
        model.addAttribute("customers", customerService.findAll(q));
        model.addAttribute("q", q == null ? "" : q);
        return "customers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitleKey", "customers.create.title");
        model.addAttribute("pageSubtitleKey", "customers.create.subtitle");
        model.addAttribute("customerForm", new CustomerForm());
        return "customers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("customerForm") CustomerForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model,
                         Locale locale) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitleKey", "customers.create.title");
            model.addAttribute("pageSubtitleKey", "customers.create.subtitle");
            return "customers/form";
        }
        try {
            Customer customer = customerService.create(form);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.customer.created", null, locale));
            return "redirect:/customers/" + customer.getId();
        } catch (IllegalArgumentException ex) {
            String key = "duplicate-phone".equals(ex.getMessage()) ? "validation.customer.phone.duplicate" : "validation.customer.phone.invalid";
            bindingResult.rejectValue("phoneNumber", "phone", messageSource.getMessage(key, null, locale));
            model.addAttribute("pageTitleKey", "customers.create.title");
            model.addAttribute("pageSubtitleKey", "customers.create.subtitle");
            return "customers/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Customer customer = customerService.getById(id);
        model.addAttribute("pageTitleKey", "customers.detail.title");
        model.addAttribute("pageSubtitleKey", "customers.detail.subtitle");
        model.addAttribute("customer", customer);
        return "customers/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Customer customer = customerService.getById(id);
        CustomerForm form = new CustomerForm();
        form.setFullName(customer.getFullName());
        form.setPhoneNumber(customer.getPhoneNumber());
        form.setSecondaryPhoneNumber(customer.getSecondaryPhoneNumber());
        form.setNotes(customer.getNotes());
        model.addAttribute("customer", customer);
        model.addAttribute("customerForm", form);
        model.addAttribute("pageTitleKey", "customers.edit.title");
        model.addAttribute("pageSubtitleKey", "customers.edit.subtitle");
        return "customers/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("customerForm") CustomerForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model,
                         Locale locale) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customer", customerService.getById(id));
            model.addAttribute("pageTitleKey", "customers.edit.title");
            model.addAttribute("pageSubtitleKey", "customers.edit.subtitle");
            return "customers/form";
        }
        try {
            customerService.update(id, form);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.customer.updated", null, locale));
            return "redirect:/customers/" + id;
        } catch (IllegalArgumentException ex) {
            String key = "duplicate-phone".equals(ex.getMessage()) ? "validation.customer.phone.duplicate" : "validation.customer.phone.invalid";
            bindingResult.rejectValue("phoneNumber", "phone", messageSource.getMessage(key, null, locale));
            model.addAttribute("customer", customerService.getById(id));
            model.addAttribute("pageTitleKey", "customers.edit.title");
            model.addAttribute("pageSubtitleKey", "customers.edit.subtitle");
            return "customers/form";
        }
    }
}
