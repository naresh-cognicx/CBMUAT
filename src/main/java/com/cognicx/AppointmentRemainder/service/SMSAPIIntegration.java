package com.cognicx.AppointmentRemainder.service;

public interface SMSAPIIntegration {

	public String sendSMS(String phoneNumber, String language,String actionId);
	
}
