package com.cognicx.AppointmentRemainder.controller;

import com.cognicx.AppointmentRemainder.Request.AgentRequest;
import com.cognicx.AppointmentRemainder.Request.AgentStatusUpdateRequest;
import com.cognicx.AppointmentRemainder.Request.CallBackScheduleRequest;
import com.cognicx.AppointmentRemainder.Request.MusicAddRequest;
import com.cognicx.AppointmentRemainder.constant.ApplicationConstant;
import com.cognicx.AppointmentRemainder.response.GenericAgentResponse;
import com.cognicx.AppointmentRemainder.response.GenericResponse;
import com.cognicx.AppointmentRemainder.service.AgentService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.omg.CORBA.portable.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;

@RestController
@CrossOrigin
@RequestMapping("/agent")
public class AgentController {

    @Autowired
    AgentService agentService;
    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);

    @PostMapping("/createCallSchedule")
    public ResponseEntity<GenericResponse> createCampaign(@RequestBody CallBackScheduleRequest callBackScheduleRequest)
            throws ParseException, IOException {
        logger.info("Invoking the Call Scheduler API : " + callBackScheduleRequest.toString());
        return agentService.createCallbackSchedule(callBackScheduleRequest);
    }
    @PostMapping("/agentAsteriskLogin")
    public ResponseEntity<GenericResponse> agentAsteriskLogin(@RequestBody AgentRequest agentRequest) {
        logger.info("Invoking the Agent Asterisk  : " + agentRequest.toString());
        return agentService.agentAsteriskMultiLogin(agentRequest);
    }
    @PostMapping("/agentAsteriskCampaignbasedLogin")
    public ResponseEntity<GenericResponse> agentAsteriskCampaignbasedLogin(@RequestBody AgentRequest agentRequest) {
        logger.info("Invoking the Agent Asterisk Login : " + agentRequest.toString());
        return agentService.agentAsteriskLogin(agentRequest);
    }
    @PostMapping("/agentAsteriskMultiLogin")
    public ResponseEntity<GenericResponse> agentAsteriskMultiLogin(@RequestBody AgentRequest agentRequest) {
        logger.info("Invoking the Agent Asterisk Login : " + agentRequest.toString());
        return agentService.agentAsteriskMultiLogin(agentRequest);
    }

    @PostMapping("/holdMusicAdd")
    public ResponseEntity<GenericResponse> holdMusicAdd(@RequestBody MusicAddRequest musicAddRequest) {
        logger.info("Invoking the hold Music Add : " + musicAddRequest.toString());
        return agentService.holdMusicAdd(musicAddRequest);
    }
    @GetMapping("/getCampaignDetailForAgent")
    public ResponseEntity<GenericResponse> getCampaignDetail(@RequestParam String userGroup)
            throws ParseException, JsonParseException, JsonMappingException, IOException {
        logger.info("Invoking Get Campaign Detail for the User Group ID :" + userGroup);
        return agentService.getCampaignDetail(userGroup);
    }
    @GetMapping("/getDialplanList")
    public ResponseEntity<GenericResponse> getDialplanList()
            throws ParseException, JsonParseException, JsonMappingException, IOException {
        return agentService.getDialplanList();
    }
    @GetMapping("/getCampaignbyDialplan")
    public ResponseEntity<GenericResponse> getCampaignbyDialplan(@RequestParam String dialplan)
            throws ParseException, JsonParseException, JsonMappingException, IOException {
        return agentService.getCampaignbyDialplan(dialplan);
    }



//    @PostMapping("/updateStatus")
//    public GenericAgentResponse <AgentStatusUpdateRequest> updateAgentStatus(@RequestBody AgentStatusUpdateRequest userStatusRequest, HttpServletRequest request) {
//        GenericAgentResponse<AgentStatusUpdateRequest> response = new GenericAgentResponse<>();
//
//        try {
//            userStatusRequest = agentService.updateAgentStatus(userStatusRequest);
//            response.setData(userStatusRequest);
//            response.setMessage(ApplicationConstant.USER_STATUS_IS_SAVED_SUCCESSFULLY);
//            response.setStatus(Response.Status.OK);
//
//        } catch (ApplicationException ex) {
//            logger.error("{}:saveUserStatus:ApplicationException:",
//                    null != userStatusRequest.getId() ? userStatusRequest.getAgentId()
//                            : userStatusRequest.getStatus(),
//                    ex);
//            response.setErrorMessages(Arrays.asList(ApplicationConstant.FAILED_TO_SAVE_USER_STATUS));
//            response.setMessage(ex.getMessage());
//            response.setStatus(ex.getStatus());
//
//        } catch (Exception e) {
//            logger.error("{}:saveUserStatus:Exception:",
//                    null != userStatusRequest.getId() ? userStatusRequest.getAgentId()
//                            : userStatusRequest.getStatus(),
//                    e);
//            response.setErrorMessages(Arrays.asList(ApplicationConstant.FAILED_TO_SAVE_USER_STATUS));
//            response.setMessage(ApplicationConstant.FAILED_TO_SAVE_USER_STATUS_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN);
//            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
//        }
//        return response;
//
//    }
}
