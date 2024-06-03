package com.cognicx.AppointmentRemainder.Dto;

import java.math.BigInteger;
import java.util.Map;

public class DynamicContactDetDto {

	
	private String contactId;
	private String campaignId;
	private String campaignName;
	private String actionId;
	private String customerMobileNumber;
	private String language;
	private String subskill_set;
	private String callRetryCount;
	private String updatedDate;
	private String callStatus;
	private String failureReason;
	private BigInteger historyId;
	private Map<String,String> mapDynamicFields;
	
	private String agent_userid;
	
	
	
	
	
	public String getAgent_userid() {
		return agent_userid;
	}
	public void setAgent_userid(String agent_userid) {
		this.agent_userid = agent_userid;
	}
	public Map<String, String> getMapDynamicFields() {
		return mapDynamicFields;
	}
	public void setMapDynamicFields(Map<String, String> mapDynamicFields) {
		this.mapDynamicFields = mapDynamicFields;
	}
	
	
	public String getContactId() {
		return contactId;
	}
	public void setContactId(String contactId) {
		this.contactId = contactId;
	}
	public String getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}
	public String getCampaignName() {
		return campaignName;
	}
	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}
	public String getActionId() {
		return actionId;
	}
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
	public String getCustomerMobileNumber() {
		return customerMobileNumber;
	}
	public void setCustomerMobileNumber(String customerMobileNumber) {
		this.customerMobileNumber = customerMobileNumber;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getSubskill_set() {
		return subskill_set;
	}
	public void setSubskill_set(String subskill_set) {
		this.subskill_set = subskill_set;
	}
	public String getCallRetryCount() {
		return callRetryCount;
	}
	public void setCallRetryCount(String callRetryCount) {
		this.callRetryCount = callRetryCount;
	}
	public String getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}
	public String getCallStatus() {
		return callStatus;
	}
	public void setCallStatus(String callStatus) {
		this.callStatus = callStatus;
	}
	public String getFailureReason() {
		return failureReason;
	}
	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}
	public BigInteger getHistoryId() {
		return historyId;
	}
	public void setHistoryId(BigInteger historyId) {
		this.historyId = historyId;
	}
	

	
	
}
