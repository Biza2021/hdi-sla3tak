package com.hdisla3tak.app.web;

import com.hdisla3tak.app.domain.RepairItem;
import com.hdisla3tak.app.service.RepairItemService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicTrackingController {

    private final RepairItemService repairItemService;

    public PublicTrackingController(RepairItemService repairItemService) {
        this.repairItemService = repairItemService;
    }

    @GetMapping("/track/{token}")
    public String track(@PathVariable String token, Model model, HttpServletResponse response) {
        RepairItem item = repairItemService.findByPublicTrackingToken(token).orElse(null);
        if (item == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "track/not-found";
        }

        model.addAttribute("item", item);
        return "track/detail";
    }
}
