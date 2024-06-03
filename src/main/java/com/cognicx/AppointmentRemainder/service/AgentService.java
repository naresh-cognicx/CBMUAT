package com.cognicx.AppointmentRemainder.service;

import com.cognicx.AppointmentRemainder.Request.AgentRequest;
import com.cognicx.AppointmentRemainder.Request.CallBackScheduleRequest;
import com.cognicx.AppointmentRemainder.Request.MusicAddRequest;
import com.cognicx.AppointmentRemainder.response.GenericResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


public interface AgentService {
    ResponseEntity<GenericResponse> createCallbackSchedule(CallBackScheduleRequest callBackScheduleRequest);

    ResponseEntity<GenericResponse> agentAsteriskLogout(AgentRequest agentRequest);

    ResponseEntity<GenericResponse> agentAsteriskLogin(AgentRequest agentRequest);

    ResponseEntity<GenericResponse> holdMusicAdd(MusicAddRequest musicAddRequest);

//    AgentStatusUpdateRequest updateAgentStatus(AgentStatusUpdateRequest userStatusRequest) throws Exception;

    ResponseEntity<GenericResponse> getCampaignDetail(String userGroup);

    ResponseEntity<GenericResponse> getDialplanList();

    ResponseEntity<GenericResponse> getCampaignbyDialplan(String dialplan);

    ResponseEntity<GenericResponse> agentAsteriskMultiLogin(AgentRequest agentRequest);
}
