package com.hdisla3tak.app.web;

import com.hdisla3tak.app.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Locale;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String dashboard(Model model, Locale locale) {
        model.addAttribute("pageTitleKey", "dashboard.title");
        model.addAttribute("pageSubtitleKey", "dashboard.subtitle");
        model.addAttribute("stats", dashboardService.getStats(locale));
        return "dashboard";
    }
}
