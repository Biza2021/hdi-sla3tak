package com.hdisla3tak.app.web.form;

import jakarta.validation.constraints.Size;

public class DeliveryForm {

    @Size(max = 120, message = "{validation.delivery.deliveredBy.size}")
    private String deliveredBy;

    @Size(max = 800, message = "{validation.delivery.note.size}")
    private String deliveryConfirmationNote;

    public String getDeliveredBy() {
        return deliveredBy;
    }

    public void setDeliveredBy(String deliveredBy) {
        this.deliveredBy = deliveredBy;
    }

    public String getDeliveryConfirmationNote() {
        return deliveryConfirmationNote;
    }

    public void setDeliveryConfirmationNote(String deliveryConfirmationNote) {
        this.deliveryConfirmationNote = deliveryConfirmationNote;
    }
}
