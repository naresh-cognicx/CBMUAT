package com.cognicx.AppointmentRemainder.controller;

import com.cognicx.AppointmentRemainder.Exception.ApplicationException;
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
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);

    @Autowired
    AgentService agentService;

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
            response.setStatus(ex.getStatus());

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

        } catch (ApplicationException ex) {
            logger.error("{}:listUserStatus:ApplicationException:", ex);
            response.setErrorMessages(Arrays.asList(ApplicationConstant.FAILED_TO_LIST_USER_STATUS));
            response.setMessage(ex.getMessage());
            response.setStatus(ex.getStatus());

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

        } catch (ApplicationException ex) {
            logger.error("{}:listNotReadyStatus:ApplicationException:", ex);
            response.setErrorMessages(Arrays.asList(ApplicationConstant.FAILED_TO_LIST_USER_STATUS));
            response.setMessage(ex.getMessage());
            response.setStatus(ex.getStatus());

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
            response.setStatus(e.getStatus());
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
        } catch (ApplicationException e) {
            logger.error("{}:agentInteractionList:ApplicationException:", agentId, e);
            response.setErrorMessages(Arrays.asList(e.getMessage()));
            response.setStatus(Response.Status.BAD_REQUEST);
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
       }
       catch (ApplicationException e) {
           logger.error("{}:updateDispositonInagentInteraction:ApplicationException:", sipId, e);
           response.setErrorMessages(Arrays.asList(e.getMessage()));
           response.setStatus(Response.Status.BAD_REQUEST);
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

  /*  @PostMapping("/createCustomer")
    public GenericAgentResponse<CustomerRequest> createCustomer(@RequestBody CustomerRequest customerRequest){
        GenericAgentResponse<CustomerRequest> response = new GenericAgentResponse<>();
        try {
            CustomerRequest customerDetail = agentService.createCustomer(customerRequest);
            if (customerDetail == null) {
                throw new ApplicationException(ApplicationConstants.MOBILE_NUMBER_NOT_FOUND, Response.Status.NOT_FOUND);
            }
            response.setData(customerDetail);
            response.setStatus(Response.Status.OK);
            response.setMessage("Successfully Created");
        } catch (ApplicationException exception) {
            logger.error(exception.getMessage());
            response.setMessage("Failed to Create");
            response.setStatus(Response.Status.BAD_REQUEST);
            response.setErrorMessages(Collections.singletonList(exception.getMessage()));
        } catch (Exception e) {
            logger.error("{}:saveuserDisposition:Exception:",
                    null != customerDetails.getCustomerId() ? customerDetails.getCustomerId()
                            : customerDetails.getMobileNumber(),
                    e);
            response.setErrorMessages(Arrays.asList(ApplicationConstants.FAILED_TO_SAVE_CUSTOMER_DETAILS));
            response.setMessage(ApplicationConstants.FAILED_TO_SAVE_CUSTOME_DETAILS_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return response;
    }*/
}
