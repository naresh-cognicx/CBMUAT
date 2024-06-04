package com.cognicx.AppointmentRemainder.Request;

import com.cognicx.AppointmentRemainder.model.AuditFields;
import lombok.Data;


@Data
public class CustomerRequest extends AuditFields {
    private String customerId;
    private String firstName;
    private String sipId;
    private String lastName;
    private String whatsappNumber;
    private String mobileNumber;
    private String emailId;
    private String smId;
    private String altGuarantorNumber;
    private String city;
    private String customerAddress;
    private String segment;
    private double dueAmount;
    private String dueDate;
    private int aging;
    private String delinquentDebtAmount;
    private String customerCategory;
    private boolean stickyNotes;
    private String comments;
    private String dispositionReason;
}
