package com.cognicx.AppointmentRemainder.dao;

import com.cognicx.AppointmentRemainder.Request.AgentRequest;
import com.cognicx.AppointmentRemainder.Request.CallBackScheduleRequest;
import com.cognicx.AppointmentRemainder.Request.MusicAddRequest;

import java.util.List;

public interface AgentDao {
    boolean createCallbackSchedule(CallBackScheduleRequest callBackScheduleRequest);

    boolean agentAsterisk(AgentRequest agentRequest);

    boolean holdMusicAdd(MusicAddRequest musicAddRequest);

    List<Object[]> getCampaignDetforAgent(String userGroup);

    List<String> getDialplanList();

    List<String> getCampaignbyDialplan(String dialplan);

    boolean agentAsteriskMultiaction(AgentRequest agentRequest);

}
