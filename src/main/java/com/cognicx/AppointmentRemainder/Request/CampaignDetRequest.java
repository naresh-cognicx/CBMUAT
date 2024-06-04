package com.cognicx.AppointmentRemainder.Request;

import java.util.List;

public class CampaignDetRequest {

	private String campaignId;
	private String campaignName;
	private String campaignActive;
	private String startDate;
	private String endDate;
	private String startTime;
	private String endTime;
	private String maxAdvNotice;
	private String retryDelay;
	private String retryCount;
	private String concurrentCall;
	private String ftpLocation;
	private String ftpUsername;
	private String ftpPassword;
	private String fileName;
	private String callBefore;
	private boolean schedulerEnabled;
	private String dncId;
	private String DailingMode;
	private String Queue;
	private String dispositionID;
	private String frontstatus;
	private String userGroup;

	private String Dailingoption;

	private String previewOption;


	private List<CampaignWeekDetRequest> weekDaysTime;

	public String getDailingMode() {
		return DailingMode;
	}

	public String getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(String userGroup) {
		this.userGroup = userGroup;
	}

	public String getPreviewOption() {
		return previewOption;
	}

	public void setPreviewOption(String previewOption) {
		this.previewOption = previewOption;
	}


	public void setDailingMode(String dailingMode) {
		DailingMode = dailingMode;
	}

	public String getQueue() {
		return Queue;
	}

	public String getDailingoption() {
		return Dailingoption;
	}

	public void setDailingoption(String dailingoption) {
		Dailingoption = dailingoption;
	}

	public void setQueue(String queue) {
		Queue = queue;
	}

	public String getDispositionID() {
		return dispositionID;
	}

	public void setDispositionID(String dispositionID) {
		this.dispositionID = dispositionID;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public String getCampaignActive() {
		return campaignActive;
	}

	public void setCampaignActive(String campaignActive) {
		this.campaignActive = campaignActive;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getMaxAdvNotice() {
		return maxAdvNotice;
	}

	public void setMaxAdvNotice(String maxAdvNotice) {
		this.maxAdvNotice = maxAdvNotice;
	}

	public String getRetryDelay() {
		return retryDelay;
	}

	public void setRetryDelay(String retryDelay) {
		this.retryDelay = retryDelay;
	}

	public String getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(String retryCount) {
		this.retryCount = retryCount;
	}

	public String getConcurrentCall() {
		return concurrentCall;
	}

	public void setConcurrentCall(String concurrentCall) {
		this.concurrentCall = concurrentCall;
	}

	public String getFtpLocation() {
		return ftpLocation;
	}

	public void setFtpLocation(String ftpLocation) {
		this.ftpLocation = ftpLocation;
	}

	public String getFtpUsername() {
		return ftpUsername;
	}

	public void setFtpUsername(String ftpUsername) {
		this.ftpUsername = ftpUsername;
	}

	public String getFtpPassword() {
		return ftpPassword;
	}

	public void setFtpPassword(String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}

	public List<CampaignWeekDetRequest> getWeekDaysTime() {
		return weekDaysTime;
	}

	public void setWeekDaysTime(List<CampaignWeekDetRequest> weekDaysTime) {
		this.weekDaysTime = weekDaysTime;
	}

	public String getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getCallBefore() {
		return callBefore;
	}

	public void setCallBefore(String callBefore) {
		this.callBefore = callBefore;
	}

	
	
	public boolean isSchedulerEnabled() {
		return schedulerEnabled;
	}

	public void setSchedulerEnabled(boolean schedulerEnabled) {
		this.schedulerEnabled = schedulerEnabled;
	}

	public String getDncId() {
		return dncId;
	}

	public void setDncId(String dncId) {
		this.dncId = dncId;
	}
	
	

	public String getFrontstatus() {
		return frontstatus;
	}

	public void setFrontstatus(String frontstatus) {
		this.frontstatus = frontstatus;
	}

	@Override
	public String toString() {
		return "CampaignDetRequest [campaignId=" + campaignId + ", campaignName=" + campaignName + ", campaignActive="
				+ campaignActive + ", startDate=" + startDate + ", endDate=" + endDate + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", maxAdvNotice=" + maxAdvNotice + ", retryDelay=" + retryDelay
				+ ", retryCount=" + retryCount + ", concurrentCall=" + concurrentCall + ", ftpLocation=" + ftpLocation
				+ ", ftpUsername=" + ftpUsername + ", ftpPassword=" + ftpPassword + ", fileName=" + fileName
				+ ", callBefore=" + callBefore + ", schedulerEnabled=" + schedulerEnabled + ", weekDaysTime="
				+ weekDaysTime + "]";
	}

	
	
}
