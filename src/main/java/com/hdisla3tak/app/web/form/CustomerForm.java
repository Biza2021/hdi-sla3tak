package com.hdisla3tak.app.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CustomerForm {

    @NotBlank(message = "{validation.customer.fullName.required}")
    @Size(max = 140, message = "{validation.customer.fullName.size}")
    private String fullName;

    @NotBlank(message = "{validation.customer.phone.required}")
    @Pattern(regexp = "^[0-9()+ -]{8,20}$", message = "{validation.customer.phone.invalid}")
    private String phoneNumber;

    @Pattern(regexp = "(^$|^[0-9()+ -]{8,20}$)", message = "{validation.customer.secondaryPhone.invalid}")
    @Size(max = 30, message = "{validation.customer.secondaryPhone.size}")
    private String secondaryPhoneNumber;

    @Size(max = 800, message = "{validation.customer.notes.size}")
    private String notes;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getSecondaryPhoneNumber() { return secondaryPhoneNumber; }
    public void setSecondaryPhoneNumber(String secondaryPhoneNumber) { this.secondaryPhoneNumber = secondaryPhoneNumber; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
