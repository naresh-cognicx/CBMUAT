package com.cognicx.AppointmentRemainder.Request;

import java.math.BigInteger;

public class UpdateCallDetRequest {
	private String actionid;
	private String phone;
	private String disposition;
	private String callduration;
	private String hangupcode;
	private int retryCount;
	private String callerResponse;
	private String surveyrating;
	private String SMS_Triggered;
	private String callStartTime;
	private String callEndTime;

	private String dialstatus;

	private String hangupreason;

	private String hanguptext;

	private String callanswer;

	private String calltalktime;

	public String getCallanswer() {
		return callanswer;
	}

	public void setCallanswer(String callanswer) {
		this.callanswer = callanswer;
	}

	public String getCalltalktime() {
		return calltalktime;
	}

	public void setCalltalktime(String calltalktime) {
		this.calltalktime = calltalktime;
	}

	public String getHanguptext() {
		return hanguptext;
	}

	public void setHanguptext(String hanguptext) {
		this.hanguptext = hanguptext;
	}

	public String getHangupreason() {
		return hangupreason;
	}

	public void setHangupreason(String hangupreason) {
		this.hangupreason = hangupreason;
	}

	public String getDialstatus() {
		return dialstatus;
	}

	public void setDialstatus(String dialstatus) {
		this.dialstatus = dialstatus;
	}

	public String getSMS_Triggered() {
		return SMS_Triggered;
	}

	public void setSMS_Triggered(String sMS_Triggered) {
		SMS_Triggered = sMS_Triggered;
	}
	private BigInteger historyId;

	public BigInteger getHistoryId() {
		return historyId;
	}

	public void setHistoryId(BigInteger historyId) {
		this.historyId = historyId;
	}
	public String getCallStartTime() {
		return callStartTime;
	}
	public void setCallStartTime(String callStartTime) {
		this.callStartTime = callStartTime;
	}

	public String getCallEndTime() {
		return callEndTime;
	}

	public void setCallEndTime(String callEndTime) {
		this.callEndTime = callEndTime;
	}

	public String getSurveyrating() {
		return surveyrating;
	}

	public void setSurveyrating(String surveyrating) {
		this.surveyrating = surveyrating;
	}

	public String getActionid() {
		return actionid;
	}
	public void setActionid(String actionid) {
		this.actionid = actionid;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getCallerResponse() {
		return callerResponse;
	}

	public void setCallerResponse(String callerResponse) {
		this.callerResponse = callerResponse;
	}

	public String getDisposition() {
		return disposition;
	}
	public void setDisposition(String disposition) {
		this.disposition = disposition;
	}


	public String getHangupcode() {
		return hangupcode;
	}
	public void setHangupcode(String hangupcode) {
		this.hangupcode = hangupcode;
	}
	public int getRetryCount() {
		return retryCount;
	}
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
	public String getCallduration() {
		return callduration;
	}
	public void setCallduration(String callduration) {
		this.callduration = callduration;
	}



}
