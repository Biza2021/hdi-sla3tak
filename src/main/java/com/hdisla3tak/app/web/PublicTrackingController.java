package com.hdisla3tak.app.web;

import com.hdisla3tak.app.domain.RepairItem;
import com.hdisla3tak.app.service.FileStorageService;
import com.hdisla3tak.app.service.RepairItemService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.PathResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicTrackingController {

    private final RepairItemService repairItemService;
    private final FileStorageService fileStorageService;

    public PublicTrackingController(RepairItemService repairItemService,
                                    FileStorageService fileStorageService) {
        this.repairItemService = repairItemService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/track/{token}")
    public String track(@PathVariable String token, Model model, HttpServletResponse response) {
        RepairItem item = repairItemService.findByPublicTrackingToken(token).orElse(null);
        if (item == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "track/not-found";
        }

        model.addAttribute("item", item);
        model.addAttribute("appDisplayName", resolveTrackingShopName(item));
        return "track/detail";
    }

    @GetMapping("/track/{token}/image")
    public ResponseEntity<PathResource> trackImage(@PathVariable String token) {
        RepairItem item = repairItemService.findByPublicTrackingToken(token).orElse(null);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        return fileStorageService.resolveImagePath(item.getImagePath(), item.getShop())
            .map(path -> ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(path.getFileName().toString()).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(new PathResource(path)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String resolveTrackingShopName(RepairItem item) {
        return item.getShop() != null && StringUtils.hasText(item.getShop().getName())
            ? item.getShop().getName().trim()
            : "Hdi Sla3tak";
    }
}
