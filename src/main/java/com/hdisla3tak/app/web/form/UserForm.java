package com.hdisla3tak.app.web.form;

import com.hdisla3tak.app.domain.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserForm {

    @NotBlank(message = "{validation.user.fullName.required}")
    @Size(max = 120, message = "{validation.user.fullName.size}")
    private String fullName;

    @NotBlank(message = "{validation.user.username.required}")
    @Size(max = 60, message = "{validation.user.username.size}")
    private String username;

    @Size(max = 100, message = "{validation.user.password.size}")
    private String password;

    @NotNull(message = "{validation.user.role.required}")
    private UserRole role;

    private boolean active = true;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
