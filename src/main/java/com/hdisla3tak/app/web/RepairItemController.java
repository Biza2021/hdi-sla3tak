package com.hdisla3tak.app.web;

import com.hdisla3tak.app.domain.RepairItem;
import com.hdisla3tak.app.domain.enums.RepairStatus;
import com.hdisla3tak.app.repository.CustomerRepository;
import com.hdisla3tak.app.service.NotificationService;
import com.hdisla3tak.app.service.RepairItemService;
import com.hdisla3tak.app.web.form.DeliveryForm;
import com.hdisla3tak.app.web.form.RepairItemForm;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.Locale;

@Controller
@RequestMapping("/items")
public class RepairItemController {

    private final RepairItemService repairItemService;
    private final NotificationService notificationService;
    private final CustomerRepository customerRepository;
    private final MessageSource messageSource;

    public RepairItemController(RepairItemService repairItemService,
                                NotificationService notificationService,
                                CustomerRepository customerRepository,
                                MessageSource messageSource) {
        this.repairItemService = repairItemService;
        this.notificationService = notificationService;
        this.customerRepository = customerRepository;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "status", required = false) RepairStatus status,
                       @RequestParam(value = "delivered", required = false) String delivered,
                       @RequestParam(value = "category", required = false) String category,
                       Model model) {
        model.addAttribute("pageTitleKey", "items.title");
        model.addAttribute("pageSubtitleKey", "items.subtitle");
        model.addAttribute("items", repairItemService.findAll(q, status, delivered, category));
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDelivered", delivered == null ? "" : delivered);
        model.addAttribute("selectedCategory", category == null ? "" : category);
        return "items/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(value = "customerId", required = false) Long customerId, Model model) {
        RepairItemForm form = new RepairItemForm();
        form.setDateReceived(LocalDate.now());
        form.setStatus(RepairStatus.RECEIVED);
        form.setCustomerId(customerId);
        model.addAttribute("repairItemForm", form);
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("pageTitleKey", "items.create.title");
        model.addAttribute("pageSubtitleKey", "items.create.subtitle");
        return "items/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("repairItemForm") RepairItemForm form,
                         BindingResult bindingResult,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         Locale locale) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", customerRepository.findAll());
            model.addAttribute("pageTitleKey", "items.create.title");
            model.addAttribute("pageSubtitleKey", "items.create.subtitle");
            return "items/form";
        }
        try {
            RepairItem item = repairItemService.create(form, imageFile, authentication.getName());
            notificationService.prepareItemRegistered(item, currentBaseUrl())
                .ifPresent(smsComposeAction -> redirectAttributes.addFlashAttribute("smsComposeAction", smsComposeAction));
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.item.created", null, locale));
            return "redirect:/items/" + item.getId();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("customers", customerRepository.findAll());
            model.addAttribute("pageTitleKey", "items.create.title");
            model.addAttribute("pageSubtitleKey", "items.create.subtitle");
            model.addAttribute("uploadError", ex.getMessage());
            return "items/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        RepairItem item = repairItemService.getById(id);
        model.addAttribute("pageTitleKey", "items.detail.title");
        model.addAttribute("pageSubtitleKey", "items.detail.subtitle");
        model.addAttribute("item", item);
        model.addAttribute(
            "trackingUrl",
            repairItemService.buildPublicTrackingUrl(
                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString(),
                item
            )
        );
        return "items/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        RepairItem item = repairItemService.getById(id);
        RepairItemForm form = new RepairItemForm();
        form.setCustomerId(item.getCustomer().getId());
        form.setCategory(item.getCategory());
        form.setTitle(item.getTitle());
        form.setDescription(item.getDescription());
        form.setDateReceived(item.getDateReceived());
        form.setRepairNotes(item.getRepairNotes());
        form.setStatus(item.getStatus());
        form.setEstimatedPrice(item.getEstimatedPrice());
        form.setDepositPaid(item.getDepositPaid());
        form.setExpectedDeliveryDate(item.getExpectedDeliveryDate());
        model.addAttribute("repairItemForm", form);
        model.addAttribute("item", item);
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("pageTitleKey", "items.edit.title");
        model.addAttribute("pageSubtitleKey", "items.edit.subtitle");
        return "items/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("repairItemForm") RepairItemForm form,
                         BindingResult bindingResult,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         Locale locale) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", customerRepository.findAll());
            model.addAttribute("item", repairItemService.getById(id));
            model.addAttribute("pageTitleKey", "items.edit.title");
            model.addAttribute("pageSubtitleKey", "items.edit.subtitle");
            return "items/form";
        }
        try {
            RepairStatus previousStatus = repairItemService.getById(id).getStatus();
            RepairItem item = repairItemService.update(id, form, imageFile, authentication.getName());
            if (previousStatus != RepairStatus.READY_FOR_PICKUP && item.getStatus() == RepairStatus.READY_FOR_PICKUP) {
                notificationService.prepareReadyForPickup(item, currentBaseUrl())
                    .ifPresent(smsComposeAction -> redirectAttributes.addFlashAttribute("smsComposeAction", smsComposeAction));
            }
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.item.updated", null, locale));
            return "redirect:/items/" + id;
        } catch (IllegalArgumentException ex) {
            model.addAttribute("customers", customerRepository.findAll());
            model.addAttribute("item", repairItemService.getById(id));
            model.addAttribute("pageTitleKey", "items.edit.title");
            model.addAttribute("pageSubtitleKey", "items.edit.subtitle");
            model.addAttribute("uploadError", ex.getMessage());
            return "items/form";
        }
    }

    @GetMapping("/delivery")
    public String deliverySearch(@RequestParam(value = "q", required = false) String q, Model model) {
        model.addAttribute("pageTitleKey", "delivery.title");
        model.addAttribute("pageSubtitleKey", "delivery.subtitle");
        model.addAttribute("results", repairItemService.searchForDelivery(q));
        model.addAttribute("q", q == null ? "" : q);
        return "items/delivery-search";
    }

    @GetMapping("/{id}/deliver")
    public String deliverForm(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitleKey", "deliver.title");
        model.addAttribute("pageSubtitleKey", "deliver.subtitle");
        model.addAttribute("item", repairItemService.getById(id));
        model.addAttribute("deliveryForm", new DeliveryForm());
        return "items/deliver";
    }

    @PostMapping("/{id}/deliver")
    public String deliver(@PathVariable Long id,
                          @Valid @ModelAttribute("deliveryForm") DeliveryForm deliveryForm,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          Locale locale) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("item", repairItemService.getById(id));
            model.addAttribute("pageTitleKey", "deliver.title");
            model.addAttribute("pageSubtitleKey", "deliver.subtitle");
            return "items/deliver";
        }
        try {
            repairItemService.deliver(id, deliveryForm, authentication.getName());
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("flash.item.delivered", null, locale));
            return "redirect:/items/" + id;
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.item.alreadyDelivered", null, locale));
            return "redirect:/items/" + id;
        }
    }

    @GetMapping("/{id}/receipt")
    public String receipt(@PathVariable Long id, Model model) {
        model.addAttribute("item", repairItemService.getById(id));
        return "items/receipt";
    }

    private String currentBaseUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }
}
