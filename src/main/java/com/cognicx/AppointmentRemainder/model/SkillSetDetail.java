package com.cognicx.AppointmentRemainder.model;

public class SkillSetDetail {

    private String skillSetName;

    private String proficency;

    private String queueId;

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public String getSkillSetName() {
        return skillSetName;
    }

    public void setSkillSetName(String skillSetName) {
        this.skillSetName = skillSetName;
    }

    public String getProficency() {
        return proficency;
    }

    public void setProficency(String proficency) {
        this.proficency = proficency;
    }
}
