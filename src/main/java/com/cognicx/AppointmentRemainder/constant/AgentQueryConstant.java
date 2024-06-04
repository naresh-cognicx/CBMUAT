package com.cognicx.AppointmentRemainder.constant;

public class AgentQueryConstant {
    public static final String INSERT_CALL_BACK_SCHEDULE = "INSERT INTO appointment_remainder.callback_schedule (scheduled_date, scheduled_time, type, digital_notification, dialplan_or_queue, campaign, agent) VALUES (:scheduledDate, :scheduledTime, :type, :digitalNotification, :dialplanOrQueue, :AssignCampaign, :Agent)";
    public static final String GET_CAMPAIGN_DET_BY_USERGROUP_FOR_AGENT = "SELECT campaign_id,name,queue_name,description,status,max_adv_time,retry_delay,retry_count,concurrent_call,start_date,start_time,campaign_det.end_date,end_time,ftp_location,ftp_credentials,file_name,dncId,groupname,DailingMode,Queue,dispositionID,Dailingoption FROM appointment_remainder.campaign_det where groupname=:groupName";
    public static final String GET_DIALPLAN_LIST = "SELECT DISTINCT Queue FROM appointment_remainder.campaign_det WHERE Queue IS NOT NULL";
    public static final String GET_CAMPAIGN_DET_BY_DIALPLAN = "SELECT name from appointment_remainder.campaign_det WHERE Queue=:Queue";
}
