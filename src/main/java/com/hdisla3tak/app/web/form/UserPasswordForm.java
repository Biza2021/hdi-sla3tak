package com.hdisla3tak.app.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserPasswordForm {

    @NotBlank(message = "{validation.user.password.required}")
    @Size(min = 6, max = 100, message = "{validation.user.password.create}")
    private String password;

    @NotBlank(message = "{validation.setup.confirmPassword.required}")
    private String confirmPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
