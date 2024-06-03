package com.cognicx.AppointmentRemainder.Request;

public class SkillsetRequest {
	private String skillsetId;
	private String skillName;
	private String Language;
	private String TimeZone;
	private String ChannelType;
	private String ServiceLevelThreshold;
	private String ServiceLevelGoal;
	private String FirstCallResolution;
	private String AbandonedRateThreshold;
	private String ShortCallThreshold;
	private String ShortAbandonedThreshold;
	private String CountAbandonedSLA;
	private String Disposition;

	private boolean forceACW;
	private String forceACWSec;
	private boolean autoanswer;
	private String autoanswersec;

	private String noAnswer;
	private String VDNQueueId;
	private String routingStrategy;

	public String getVDNQueueId() {
		return VDNQueueId;
	}

	public void setVDNQueueId(String VDNQueueId) {
		this.VDNQueueId = VDNQueueId;
	}

	public boolean isForceACW() {
		return forceACW;
	}

	public void setForceACW(boolean forceACW) {
		this.forceACW = forceACW;
	}

	public String getForceACWSec() {
		return forceACWSec;
	}

	public void setForceACWSec(String forceACWSec) {
		this.forceACWSec = forceACWSec;
	}

	public boolean isAutoanswer() {
		return autoanswer;
	}

	public void setAutoanswer(boolean autoanswer) {
		this.autoanswer = autoanswer;
	}

	public String getAutoanswersec() {
		return autoanswersec;
	}

	public void setAutoanswersec(String autoanswersec) {
		this.autoanswersec = autoanswersec;
	}

	public String getNoAnswer() {
		return noAnswer;
	}

	public void setNoAnswer(String noAnswer) {
		this.noAnswer = noAnswer;
	}

	public String getRoutingStrategy() {
		return routingStrategy;
	}

	public void setRoutingStrategy(String routingStrategy) {
		this.routingStrategy = routingStrategy;
	}

	public String getSkillsetId() {
		return skillsetId;
	}
	public void setSkillsetId(String skillsetId) {
		this.skillsetId = skillsetId;
	}
	public String getSkillName() {
		return skillName;
	}
	public void setSkillName(String skillName) {
		this.skillName = skillName;
	}
	
	public String getDisposition() {
		return Disposition;
	}
	public void setDisposition(String disposition) {
		Disposition = disposition;
	}
	public String getLanguage() {
		return Language;
	}
	public void setLanguage(String language) {
		Language = language;
	}
	public String getTimeZone() {
		return TimeZone;
	}
	public void setTimeZone(String timeZone) {
		TimeZone = timeZone;
	}
	public String getChannelType() {
		return ChannelType;
	}
	public void setChannelType(String channelType) {
		ChannelType = channelType;
	}
	public String getServiceLevelThreshold() {
		return ServiceLevelThreshold;
	}
	public void setServiceLevelThreshold(String serviceLevelThreshold) {
		ServiceLevelThreshold = serviceLevelThreshold;
	}
	public String getServiceLevelGoal() {
		return ServiceLevelGoal;
	}
	public void setServiceLevelGoal(String serviceLevelGoal) {
		ServiceLevelGoal = serviceLevelGoal;
	}
	public String getFirstCallResolution() {
		return FirstCallResolution;
	}
	public void setFirstCallResolution(String firstCallResolution) {
		FirstCallResolution = firstCallResolution;
	}
	public String getAbandonedRateThreshold() {
		return AbandonedRateThreshold;
	}
	public void setAbandonedRateThreshold(String abandonedRateThreshold) {
		AbandonedRateThreshold = abandonedRateThreshold;
	}
	public String getShortCallThreshold() {
		return ShortCallThreshold;
	}
	public void setShortCallThreshold(String shortCallThreshold) {
		ShortCallThreshold = shortCallThreshold;
	}
	public String getShortAbandonedThreshold() {
		return ShortAbandonedThreshold;
	}
	public void setShortAbandonedThreshold(String shortAbandonedThreshold) {
		ShortAbandonedThreshold = shortAbandonedThreshold;
	}
	public String getCountAbandonedSLA() {
		return CountAbandonedSLA;
	}
	public void setCountAbandonedSLA(String countAbandonedSLA) {
		CountAbandonedSLA = countAbandonedSLA;
	}

	@Override
	public String toString() {
		return "SkillsetRequest{" +
				"skillsetId='" + skillsetId + '\'' +
				", skillName='" + skillName + '\'' +
				", Language='" + Language + '\'' +
				", TimeZone='" + TimeZone + '\'' +
				", ChannelType='" + ChannelType + '\'' +
				", ServiceLevelThreshold='" + ServiceLevelThreshold + '\'' +
				", ServiceLevelGoal='" + ServiceLevelGoal + '\'' +
				", FirstCallResolution='" + FirstCallResolution + '\'' +
				", AbandonedRateThreshold='" + AbandonedRateThreshold + '\'' +
				", ShortCallThreshold='" + ShortCallThreshold + '\'' +
				", ShortAbandonedThreshold='" + ShortAbandonedThreshold + '\'' +
				", CountAbandonedSLA='" + CountAbandonedSLA + '\'' +
				", Disposition='" + Disposition + '\'' +
				", forceACW=" + forceACW +
				", forceACWSec='" + forceACWSec + '\'' +
				", autoanswer=" + autoanswer +
				", autoanswersec='" + autoanswersec + '\'' +
				", noAnswer='" + noAnswer + '\'' +
				", VDNQueueId='" + VDNQueueId + '\'' +
				", routingStrategy='" + routingStrategy + '\'' +
				'}';
	}
}
