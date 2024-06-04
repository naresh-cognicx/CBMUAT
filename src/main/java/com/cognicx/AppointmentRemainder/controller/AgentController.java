package com.cognicx.AppointmentRemainder.controller;


import com.cognicx.AppointmentRemainder.Exception.ApplicationException;
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


import com.cognicx.AppointmentRemainder.Request.*;
import com.cognicx.AppointmentRemainder.constant.ApplicationConstant;
import com.cognicx.AppointmentRemainder.constant.StatusCodeConstants;
import com.cognicx.AppointmentRemainder.model.AgentStatusUpdate;
import com.cognicx.AppointmentRemainder.response.GenericAgentResponse;
import com.cognicx.AppointmentRemainder.response.GenericResponse;
import com.cognicx.AppointmentRemainder.response.Response;
import com.cognicx.AppointmentRemainder.service.AgentService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        return agentService.agentAsteriskCampaignbasedLogin(agentRequest);
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

   @PostMapping("/updateStatus")
    public GenericAgentResponse<AgentStatusUpdateRequest> updateAgentStatus(@RequestBody AgentStatusUpdateRequest userStatusRequest, HttpServletRequest request) {
        GenericAgentResponse<AgentStatusUpdateRequest> response = new GenericAgentResponse<>();

        try {
            userStatusRequest = agentService.updateAgentStatus(userStatusRequest);
            response.setData(userStatusRequest);
            response.setMessage(ApplicationConstant.USER_STATUS_IS_SAVED_SUCCESSFULLY);
            response.setStatus(Response.Status.OK);

        } catch (ApplicationException ex) {
            logger.error("{}:saveUserStatus:ApplicationException:",
                    null != userStatusRequest.getId() ? userStatusRequest.getAgentId()
                            : userStatusRequest.getStatus(),
                    ex);
            response.setErrorMessages(Arrays.asList(ApplicationConstant.FAILED_TO_SAVE_USER_STATUS));
            response.setMessage(ex.getMessage());

        } catch (Exception e) {
            logger.error("{}:saveUserStatus:Exception:",
                    null != userStatusRequest.getId() ? userStatusRequest.getAgentId()
                            : userStatusRequest.getStatus(),
                    e);
            response.setErrorMessages(Arrays.asList(ApplicationConstant.FAILED_TO_SAVE_USER_STATUS));
            response.setMessage(ApplicationConstant.FAILED_TO_SAVE_USER_STATUS_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return response;

    }

    @GetMapping("/userStatus/list")
    public GenericAgentResponse<AgentStatusRequest> listUserStatus(){
        GenericAgentResponse<AgentStatusRequest> response = new GenericAgentResponse<>();
        try {
            List<AgentStatusRequest> listUserStatus = agentService.getAgentStatusList();
            response.setDataList(listUserStatus);
            response.setMessage(StatusCodeConstants.SUCCESS_STR);
            response.setStatus(Response.Status.OK);

        } catch (Exception e) {
            logger.error("{}:listUserStatus:Exception:", e);
            response.setErrorMessages(Arrays.asList(ApplicationConstant.FAILED_TO_LIST_USER_STATUS));
            response.setMessage(ApplicationConstant.FAILED_TO_LIST_USER_STATUS_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR);

        }

        return response;
    }
    @GetMapping("/notReady/list")
    public GenericAgentResponse<NotReadyRequest> listNotReadyStatus(){
        GenericAgentResponse<NotReadyRequest> response = new GenericAgentResponse<>();
        try {
            List<NotReadyRequest> listUserStatus = agentService.getNotReadyStatusList();
            response.setDataList(listUserStatus);
            response.setMessage(StatusCodeConstants.SUCCESS_STR);
            response.setStatus(Response.Status.OK);

        } catch (Exception e) {
            logger.error("{}:listNotReadyStatus:Exception:", e);
            response.setErrorMessages(Arrays.asList(ApplicationConstant.FAILED_TO_LIST_USER_STATUS));
            response.setMessage(ApplicationConstant.FAILED_TO_LIST_USER_STATUS_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR);

        }

        return response;
    }
    @PostMapping("/agentinteraction")
    public GenericAgentResponse<AgentInteractionRequest> saveorupdateAgentInteraction(@RequestBody AgentInteractionRequest agentInteraction) {
        GenericAgentResponse<AgentInteractionRequest> response = new GenericAgentResponse<>();
        try {

            AgentInteractionRequest agentInteractiondata  = agentService.saveAgentInteraction(agentInteraction);
            response.setStatusCode(200);
            response.setData(agentInteractiondata);
            response.setMessage("Agent interaction saved successfully");
            response.setStatus(Response.Status.OK);
        } catch (ApplicationException e) {
            logger.error("{}:agentInteraction:ApplicationException:", agentInteraction, e);
            response.setErrorMessages(Arrays.asList(e.getMessage()));
//            response.setStatus(e.());
        } catch (Exception e) {
            logger.error("{}:agentInteraction:ApplicationException:", agentInteraction, e);
            response.setErrorMessages(Arrays.asList(e.getMessage()));
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @PostMapping("/agentinteraction/list")
    public GenericAgentResponse<List<AgentInteractionRequest>> getListofAgentSummary(@RequestParam String agentId) throws Exception {
        GenericAgentResponse<List<AgentInteractionRequest>> response = new GenericAgentResponse<>();
        List<AgentInteractionRequest> agentInteractionList = new ArrayList<>();
        try {
            agentInteractionList = agentService.getAgentInteractionList(agentId);
            response.setDataList(agentInteractionList);
            response.setStatus(Response.Status.OK);
            response.setMessage("Fetched Successfully");
            response.setSize(agentInteractionList.size());
            response.setStatusCode(200);
        } catch (Exception e) {
            logger.error("{}:agentInteraction:Exception:", agentId, e);
            throw new RuntimeException();
        }
        return response;
    }
    @PostMapping("/updateUserDisposition")
    public GenericAgentResponse<String>  updateDispositonInagentInteraction(@RequestParam String sipId, @RequestParam String disposition){
       GenericAgentResponse<String> response = new GenericAgentResponse<>();
       String status =null;
       try {
           status = agentService.updateDispositonInagentInteraction(sipId, disposition);
           if (!status.isEmpty() && status.equalsIgnoreCase("updated")) {
               response.setData(status);
               response.setStatus(Response.Status.OK);
               response.setMessage("updated Successfully");
           }
       } catch (Exception e) {
           logger.error("{}:agentInteraction:Exception:", sipId, e);
           throw new RuntimeException();
       }
        return response;
    }

    @PostMapping("/activity")
    public GenericAgentResponse<AgentActivityRequest> SaveAgentActivitytoInteractiontable(@RequestBody AgentActivityRequest activityDetails) {
        GenericAgentResponse<AgentActivityRequest> response = new GenericAgentResponse<>();
        try {
            AgentActivityRequest agentActivity = new AgentActivityRequest();
            agentActivity = agentService.saveAgentActToInteraction(activityDetails);
            response.setStatus(Response.Status.OK);
            response.setData(agentActivity);
            response.setStatusCode(200);
            response.setMessage("Activity updated in agent interaction table");
        } catch (ApplicationException e) {
            logger.error("{}:activity:ApplicationException:", activityDetails, e);
            response.setErrorMessages(Arrays.asList(e.getMessage()));
            response.setStatus(Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("{}:activity:Exception:", activityDetails, e);
            throw new RuntimeException(e);
        }
        return response;
    }
}
