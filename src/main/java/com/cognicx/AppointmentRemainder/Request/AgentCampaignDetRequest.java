package com.cognicx.AppointmentRemainder.Request;

public class AgentCampaignDetRequest {
    private String campaignId;

    private String campaignName;

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

    @Override
    public String toString() {
        return "AgentCampaignDetRequest{" +
                "campaignId='" + campaignId + '\'' +
                ", campaignName='" + campaignName + '\'' +
                '}';
    }
}
