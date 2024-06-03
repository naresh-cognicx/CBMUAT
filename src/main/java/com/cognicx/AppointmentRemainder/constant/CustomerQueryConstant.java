package com.cognicx.AppointmentRemainder.constant;

public class CustomerQueryConstant {
    public static final String INSERT_CUSTOMER = "insert into dbo.customer (customerId,firstName,firstName,lastName,whatsappNumber,mobileNumber,emailId,smId,altGuarantorNumber,city,customerAddress,segment,dueAmount,dueDate,aging,delinquentDebtAmount,customerCategory,stickyNotes,comments,dispositionReason) values(:customerId,:firstName,:firstName,:lastName,:whatsappNumber,:mobileNumber,:emailId,:smId,:altGuarantorNumber,:city,:customerAddress,:segment,:dueAmount,:dueDate,:aging,:delinquentDebtAmount,:customerCategory,:stickyNotes,:comments,:dispositionReason)";

}
