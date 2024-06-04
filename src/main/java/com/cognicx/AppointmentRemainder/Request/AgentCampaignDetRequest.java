package com.cognicx.AppointmentRemainder.Request;

public class AgentCampaignDetRequest {
    private String campaignId;

    private String campaignName;

    private String vdnQueueId;

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

    public String getVdnQueueId() {
        return vdnQueueId;
    }

    public void setVdnQueueId(String vdnQueueId) {
        this.vdnQueueId = vdnQueueId;
    }

    @Override
    public String toString() {
        return "AgentCampaignDetRequest{" +
                "campaignId='" + campaignId + '\'' +
                ", campaignName='" + campaignName + '\'' +
                ", vdnQueueId='" + vdnQueueId + '\'' +
                '}';
    }
}
