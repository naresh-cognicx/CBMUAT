package com.cognicx.AppointmentRemainder.dao.impl;

import com.cognicx.AppointmentRemainder.Request.AgentRequest;
import com.cognicx.AppointmentRemainder.Request.CallBackScheduleRequest;
import com.cognicx.AppointmentRemainder.Request.MusicAddRequest;
import com.cognicx.AppointmentRemainder.constant.AgentQueryConstant;
import com.cognicx.AppointmentRemainder.constant.ApplicationConstant;
import com.cognicx.AppointmentRemainder.constant.CampaignQueryConstant;
import com.cognicx.AppointmentRemainder.constant.SkillsetQueryConstant;
import com.cognicx.AppointmentRemainder.dao.AgentDao;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Repository("AgentDao")
@Transactional
public class AgentDaoImpl implements AgentDao {

    private final Logger logger = LoggerFactory.getLogger(AgentDaoImpl.class);

    @Value("{agentStatusApi}")
    private String agentStatusApi;

    @Value("{holdmusicadd}")
    private String holdmusicadd;

    @PersistenceContext(unitName = ApplicationConstant.FIRST_PERSISTENCE_UNIT_NAME)
    public EntityManager firstEntityManager;
    @Override
    public boolean createCallbackSchedule(CallBackScheduleRequest callBackScheduleRequest) {
        int insertVal;
        boolean isCreated = false;
        try {
            Query queryObj = firstEntityManager.createNativeQuery(AgentQueryConstant.INSERT_CALL_BACK_SCHEDULE);
            queryObj.setParameter("scheduledDate", callBackScheduleRequest.getScheduledDate());
            queryObj.setParameter("scheduledTime", callBackScheduleRequest.getScheduledTime());
            queryObj.setParameter("type", callBackScheduleRequest.getType());
            queryObj.setParameter("digitalNotification", callBackScheduleRequest.getDigitalNotification());
            queryObj.setParameter("dialplanOrQueue", callBackScheduleRequest.getDialplanOrQueue());
            queryObj.setParameter("AssignCampaign", callBackScheduleRequest.getCampaign());
            queryObj.setParameter("Agent", callBackScheduleRequest.getAgent());

            insertVal = queryObj.executeUpdate();
            logger.info("Update value : "+insertVal);
            if (insertVal > 0) {
                isCreated = true;
            }
        } catch (Exception e) {
            logger.error("Error occurred in AgentDaoImpl::createCallbackSchedule: ", e);
            isCreated = false;
        }
        return isCreated;
    }

    @Override
    public boolean agentAsterisk(AgentRequest agentRequest) {
        boolean isUpdate;

        String jsonPayload = "{\n" +
                "    \"queue\": \"" + agentRequest.getQueueId() + "\",\n" +
                "    \"agent\": \"" + agentRequest.getPbxExt() + "\",\n" +
                "    \"agentname\": \"" + agentRequest.getAgentName() + "\",\n" +
                "    \"reason\": \"" + agentRequest.getReason() + "\",\n" +
                "    \"action\": \"" + agentRequest.getAction() + "\",\n" +
                "    \"actionid\": \"" + agentRequest.getPbxExt() + "\"\n" +
                "}";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(agentStatusApi);
            httpPost.setHeader("Content-Type", "application/json");
            StringEntity requestEntity = new StringEntity(jsonPayload);
            httpPost.setEntity(requestEntity);
            logger.info("Request : " + " URL : " + httpPost + " payload : " + requestEntity + " request payload : " + jsonPayload);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 200) {
                    isUpdate = true;
                } else {
                    isUpdate = false;
                }
                logger.info("Response Status Code: " + response.getStatusLine().getStatusCode());
                if (responseEntity != null) {
                    String responseBody = EntityUtils.toString(responseEntity);
                    logger.info(responseBody);
                }
            } catch (Exception e) {
                StringWriter str = new StringWriter();
                e.printStackTrace(new PrintWriter(str));
                logger.error("Exception :" + str.toString());
                isUpdate = false;
            }
        } catch (Exception e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            logger.error("Exception :" + str.toString());
            isUpdate = false;
        }
        return isUpdate;
    }

    @Override
    public boolean holdMusicAdd(MusicAddRequest musicAddRequest) {

        boolean isUpdate = false;

        String actionId = getActionSequence();
        boolean success = createHoldMusicAddinDB(musicAddRequest,actionId);

        if (success){
            logger.info("Success");
        }else {
            logger.info("Failure");
        }

        String jsonPayload = "{\n" +
                "    \"actionid\":\""+actionId+"\",\n" +
                "    \"name\":\""+musicAddRequest.getName()+"\",\n" +
                "    \"mode\":\""+musicAddRequest.getMode()+"\",\n" +
                "    \"entry\":\""+musicAddRequest.getUrl()+"\",\n" +
                "    \"customer_code\":\""+"tenantID"+"\"\n" +
                "}";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(holdmusicadd);
            httpPost.setHeader("Content-Type", "application/json");
            StringEntity requestEntity = new StringEntity(jsonPayload);
            httpPost.setEntity(requestEntity);
            logger.info("Request : " + " URL : " + httpPost + " payload : " + requestEntity + " request payload : " + jsonPayload);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 200) {
                    isUpdate = true;
                } else {
                    isUpdate = false;
                }
                logger.info("Response Status Code: " + response.getStatusLine().getStatusCode());
                if (responseEntity != null) {
                    String responseBody = EntityUtils.toString(responseEntity);
                    logger.info(responseBody);
                }
            } catch (Exception e) {
                StringWriter str = new StringWriter();
                e.printStackTrace(new PrintWriter(str));
                logger.error("Exception :" + str.toString());
                isUpdate = false;
            }
        } catch (Exception e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            logger.error("Exception :" + str.toString());
            isUpdate = false;
        }
        return isUpdate;
    }

    private boolean createHoldMusicAddinDB(MusicAddRequest musicAddRequest,String actionId) {
        boolean isCreated = false;
        int insert = 0;
        try {
            Query queryObj = firstEntityManager.createNativeQuery("");

            insert = queryObj.executeUpdate();
            if (insert>0) {
                isCreated = true;
            }else {
                isCreated=false;
            }
        } catch (Exception e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            logger.error("Error occured in CampaignDaoImpl::getActionSequence" + str.toString());
            isCreated = false;
        }
        return isCreated;
    }

    private String getActionSequence() {
        String seq = null;
        try {
            Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_ACTIONID_SEQUENCE);
            Object objResult = queryObj.getSingleResult();
            seq = String.valueOf(objResult);
        } catch (Exception e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            logger.error("Error occured in CampaignDaoImpl::getActionSequence" + str.toString());
        }
        return seq;
    }

    @Override
    public List<Object[]> getCampaignDetforAgent(String userGroup) {
        List<Object[]> resultList = null;
        try {
            Query queryObj = firstEntityManager.createNativeQuery(AgentQueryConstant.GET_CAMPAIGN_DET_BY_USERGROUP_FOR_AGENT);
            queryObj.setParameter("groupName", userGroup);
            resultList = queryObj.getResultList();
        } catch (Exception e) {
            logger.error("Error occured in AgentDaoImpl::getCampaignDetforAgent" + e);
            return resultList;
        }
        return resultList;
    }

    @Override
    public List<String> getCampaignbyDialplan(String dialplan) {
        List<String> resultList = null;
        try {
            Query queryObj = firstEntityManager.createNativeQuery(AgentQueryConstant.GET_CAMPAIGN_DET_BY_DIALPLAN);
            queryObj.setParameter("Queue", dialplan);
            resultList = queryObj.getResultList();
        } catch (Exception e) {
            logger.error("Error occured in AgentDaoImpl::getCampaignbyDialplan" + e);
            return resultList;
        }
        return resultList;
    }

    @Override
    public boolean agentAsteriskMultiaction(AgentRequest agentRequest) {
        boolean isUpdate = false;
        String jsonPayload = null;
        List<String> queueList = new ArrayList<>();
        try {
            Query queryObj1 = firstEntityManager.createNativeQuery("SELECT QueueId from appointment_remainder.agent_skillset_mapping where PbxExt=:PbxExt");
            queryObj1.setParameter("PbxExt", agentRequest.getPbxExt());
            queueList = queryObj1.getResultList();
        }catch (Exception e){
            logger.info("Error on getting Queue Id for this Agent pbxExt : " +agentRequest.getPbxExt());
        }

        logger.info("Queue List for this Agent PbxExt : "+agentRequest.getPbxExt()+" is "+queueList);
        for (String queueId : queueList){
             jsonPayload= "{\n" +
                    "    \"queue\": \"" + queueId + "\",\n" +
                    "    \"agent\": \"" + agentRequest.getPbxExt() + "\",\n" +
                    "    \"agentname\": \"" + agentRequest.getAgentName() + "\",\n" +
                    "    \"reason\": \"" + agentRequest.getReason() + "\",\n" +
                    "    \"action\": \"" + agentRequest.getAction() + "\",\n" +
                    "    \"actionid\": \"" + agentRequest.getPbxExt() + "\"\n" +
                    "}";
    }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(agentStatusApi);
            httpPost.setHeader("Content-Type", "application/json");
            StringEntity requestEntity = new StringEntity(jsonPayload);
            httpPost.setEntity(requestEntity);
            logger.info("Request : " + " URL : " + httpPost + " payload : " + requestEntity + " request payload : " + jsonPayload);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 200) {
                    isUpdate = true;
                } else {
                    isUpdate = false;
                }
                logger.info("Response Status Code: " + response.getStatusLine().getStatusCode());
                if (responseEntity != null) {
                    String responseBody = EntityUtils.toString(responseEntity);
                    logger.info(responseBody);
                }
            } catch (Exception e) {
                StringWriter str = new StringWriter();
                e.printStackTrace(new PrintWriter(str));
                logger.error("Exception :" + str.toString());
                isUpdate = false;
            }
        } catch (Exception e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            logger.error("Exception :" + str.toString());
            isUpdate = false;
        }
        return isUpdate;
    }

    @Override
    public List<String> getDialplanList() {
        List<String> resultList = new ArrayList<>();
        try {
            Query queryObj = firstEntityManager.createNativeQuery(AgentQueryConstant.GET_DIALPLAN_LIST);
            resultList = queryObj.getResultList();
        } catch (Exception e) {
            logger.error("Error occurred in AgentDaoImpl::getDialplanList", e);
        }
        return resultList;
    }

}
