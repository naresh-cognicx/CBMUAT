package com.cognicx.AppointmentRemainder.Dto;

public class RetryDetailsDet {

	private String callRetryId;
	private String contactId;
	private String callStatus;
	private String recAddedDate;
	private String retryCount;
	private String callDuration;

	private String phoneno;

	public String getPhoneno() {
		return phoneno;
	}

	public void setPhoneno(String phoneno) {
		this.phoneno = phoneno;
	}

	public String getCallRetryId() {
		return callRetryId;
	}
	public void setCallRetryId(String callRetryId) {
		this.callRetryId = callRetryId;
	}
	public String getContactId() {
		return contactId;
	}
	public void setContactId(String contactId) {
		this.contactId = contactId;
	}
	public String getCallStatus() {
		return callStatus;
	}
	public void setCallStatus(String callStatus) {
		this.callStatus = callStatus;
	}
	public String getRecAddedDate() {
		return recAddedDate;
	}
	public void setRecAddedDate(String recAddedDate) {
		this.recAddedDate = recAddedDate;
	}
	public String getRetryCount() {
		return retryCount;
	}
	public void setRetryCount(String retryCount) {
		this.retryCount = retryCount;
	}
	public String getCallDuration() {
		return callDuration;
	}
	public void setCallDuration(String callDuration) {
		this.callDuration = callDuration;
	}
	
}
