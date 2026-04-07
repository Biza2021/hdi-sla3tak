package com.hdisla3tak.app.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BusinessNameForm {

    @NotBlank(message = "{validation.settings.businessName.required}")
    @Size(max = 160, message = "{validation.settings.businessName.size}")
    private String businessName;

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }
}
