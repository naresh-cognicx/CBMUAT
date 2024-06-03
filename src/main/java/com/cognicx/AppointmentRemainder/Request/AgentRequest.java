package com.cognicx.AppointmentRemainder.Request;

public class AgentRequest {
    private String queueId;
    private String pbxExt;
    private String agentName;
    private String action;
    private String reason;
    private String actionId;



    public String getQueueId() {
        return queueId;
    }
    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public String getPbxExt() {
        return pbxExt;
    }

    public void setPbxExt(String pbxExt) {
        this.pbxExt = pbxExt;
    }
    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    @Override
    public String toString() {
        return "AgentRequest{" +
                "queueId='" + queueId + '\'' +
                ", pbxExt='" + pbxExt + '\'' +
                ", agentName='" + agentName + '\'' +
                ", action='" + action + '\'' +
                ", reason='" + reason + '\'' +
                ", actionId='" + actionId + '\'' +
                '}';
    }
}
