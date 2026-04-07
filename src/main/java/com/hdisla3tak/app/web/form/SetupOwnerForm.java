package com.hdisla3tak.app.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SetupOwnerForm {

    @NotBlank(message = "{validation.settings.businessName.required}")
    @Size(max = 160, message = "{validation.settings.businessName.size}")
    private String businessName;

    @NotBlank(message = "{validation.user.fullName.required}")
    @Size(max = 120, message = "{validation.user.fullName.size}")
    private String fullName;

    @NotBlank(message = "{validation.user.username.required}")
    @Size(max = 60, message = "{validation.user.username.size}")
    private String username;

    @NotBlank(message = "{validation.user.password.required}")
    @Size(min = 6, max = 100, message = "{validation.user.password.create}")
    private String password;

    @NotBlank(message = "{validation.setup.confirmPassword.required}")
    private String confirmPassword;

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
