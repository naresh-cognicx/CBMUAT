package com.cognicx.AppointmentRemainder.service;

import com.cognicx.AppointmentRemainder.Request.*;
import com.cognicx.AppointmentRemainder.response.GenericResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;


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

    //CustomerRequest createCustomer(CustomerRequest customerRequest);
    AgentStatusUpdateRequest updateAgentStatus(AgentStatusUpdateRequest userStatusRequest) throws Exception;

    List<AgentStatusRequest> getAgentStatusList();

    List<NotReadyRequest> getNotReadyStatusList();

    AgentInteractionRequest saveAgentInteraction(AgentInteractionRequest agentInteraction) throws Exception;

    List<AgentInteractionRequest> getAgentInteractionList(String agentId);

    String updateDispositonInagentInteraction(String sipId, String disposition);
}
