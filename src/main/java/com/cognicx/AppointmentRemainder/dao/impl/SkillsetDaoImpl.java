package com.cognicx.AppointmentRemainder.dao.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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

import com.cognicx.AppointmentRemainder.Request.SkillsetRequest;
import com.cognicx.AppointmentRemainder.constant.ApplicationConstant;
import com.cognicx.AppointmentRemainder.constant.CampaignQueryConstant;
import com.cognicx.AppointmentRemainder.constant.SkillsetQueryConstant;
import com.cognicx.AppointmentRemainder.dao.SkillsetDao;

@Repository("SkillsetDao")
@Transactional
public class SkillsetDaoImpl implements SkillsetDao {
	private final Logger logger = LoggerFactory.getLogger(SkillsetDaoImpl.class);
	@PersistenceContext(unitName = ApplicationConstant.FIRST_PERSISTENCE_UNIT_NAME)
	public EntityManager firstEntityManager;

	@Value("queueCreateAPI")
	private String queueCreateAPI;
	@Override
	public String createSkillset(SkillsetRequest skillSetRequest) throws Exception {
		String skillSetId = null;
		boolean isInserted;
		int insertVal;
		try {
			int idValue = getskillSetId();
			if (idValue > 9)
				skillSetId = "S_" + String.valueOf(idValue);
			else
				skillSetId = "S_0" + String.valueOf(idValue);
			Query queryObj = firstEntityManager.createNativeQuery(SkillsetQueryConstant.INSERT_SKILLSET_DET);
			queryObj.setParameter("skillsetId", skillSetId);
			queryObj.setParameter("skillName", skillSetRequest.getSkillName());
			queryObj.setParameter("Language", skillSetRequest.getLanguage());
			queryObj.setParameter("TimeZone", skillSetRequest.getTimeZone());
			queryObj.setParameter("ChannelType", skillSetRequest.getChannelType());
			queryObj.setParameter("ServiceLevelThreshold", skillSetRequest.getServiceLevelThreshold());
			queryObj.setParameter("ServiceLevelGoal", skillSetRequest.getServiceLevelGoal());
			queryObj.setParameter("FirstCallResolution", skillSetRequest.getFirstCallResolution());
			queryObj.setParameter("AbandonedRateThreshold", skillSetRequest.getAbandonedRateThreshold());
			queryObj.setParameter("ShortCallThreshold", skillSetRequest.getShortCallThreshold());
			queryObj.setParameter("ShortAbandonedThreshold", skillSetRequest.getShortAbandonedThreshold());
			queryObj.setParameter("CountAbandonedSLA", skillSetRequest.getCountAbandonedSLA());
			queryObj.setParameter("Disposition", skillSetRequest.getDisposition());

			insertVal = queryObj.executeUpdate();
			if (insertVal > 0) {
				boolean success = createQueuebySkillSet(skillSetRequest);
				if (success) {
					skillSetRequest.setSkillsetId(skillSetId);
					return skillSetId;
				}
			}
		} catch (Exception e) {
			logger.error("Error occured in SkillsetDaoImpl::createSkillset" + e);
			return null;
		}
		return null;
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
	private boolean createQueuebySkillSet(SkillsetRequest skillSetRequest) {
		String actionId = "";
		try {
			Query queryObj = firstEntityManager.createNativeQuery("INSERT INTO appointment_remainder.queue_Skillset (" +
					"actionid, name, queue_name, customer_code, musiconhold, timeout, ringinuse, " +
					"retry, wrapuptime, autofill, maxlen, strategy, joinempty, leavewhenempty, " +
					"reportholdtime, announcementstatus, annoucement_path) " +
					"VALUES (:actionid, :name, :queue_name, :customer_code, :musiconhold, :timeout, :ringinuse, " +
					":retry, :wrapuptime, :autofill, :maxlen, :strategy, :joinempty, :leavewhenempty, " +
					":reportholdtime, :announcementstatus, :annoucement_path)");

			actionId = getActionSequence();

			/*
			1) Least Recent (leastrecent)
			2) Fewest Calls (fewestcalls)
			3) Linear (linear)
			4) Round Robin-M (rrmemory)
			 */

			queryObj.setParameter("actionid", actionId);
			queryObj.setParameter("name", skillSetRequest.getSkillsetId()); // queue id
			queryObj.setParameter("queue_name", skillSetRequest.getSkillName());
			queryObj.setParameter("customer_code", "tenantId");
			queryObj.setParameter("musiconhold", "test");
			queryObj.setParameter("timeout", 120);
			queryObj.setParameter("ringinuse", "no");
			queryObj.setParameter("retry", 5);
			queryObj.setParameter("wrapuptime", 5);
			queryObj.setParameter("autofill", "no");
			queryObj.setParameter("maxlen", 120);
			queryObj.setParameter("strategy", skillSetRequest.getRoutingStrategy());
			queryObj.setParameter("joinempty", "yes");
			queryObj.setParameter("leavewhenempty", "no");
			queryObj.setParameter("reportholdtime", "no");
			queryObj.setParameter("announcementstatus", "yes");
			queryObj.setParameter("annoucement_path", "www.test.com/announcement");

			int insertVal = queryObj.executeUpdate();
			if (insertVal > 0) {
				logger.info("Queue for this skillset is inserted , Queue name : " + skillSetRequest.getSkillName());
			}

		} catch (Exception e) {
			logger.error("Error on the insert queue value in the database");
		}

		boolean isCreated = false;
		String status = "Failure";
		String jsonPayload = "{\n" +
				"    \"actionid\": \"" + actionId + "\",\n" +
				"    \"name\": \"" + skillSetRequest.getSkillsetId() + "\",\n" +
				"    \"queue_name\": \"" + skillSetRequest.getSkillName() + "\",\n" +
				"    \"customer_code\": \"test\",\n" + //tenantID
				"    \"musiconhold\": \"test\",\n" +
				"    \"timeout\": 120,\n" +
				"    \"ringinuse\": \"no\",\n" +
				"    \"retry\": 5,\n" +
				"    \"wrapuptime\": 5,\n" +
				"    \"autofill\": \"yes\",\n" +
				"    \"maxlen\": 120,\n" +
				"    \"strategy\": \""+skillSetRequest.getRoutingStrategy()+"\",\n" +
				"    \"joinempty\": \"yes\",\n" +
				"    \"leavewhenempty\": \"no\",\n" +
				"    \"reportholdtime\": \"no\",\n" +
				"    \"announcementstatus\": \"yes\",\n" +
				"    \"annoucement_path\": \"www.test.com/annoucement\"\n" +
				"}";

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost(queueCreateAPI);
			httpPost.setHeader("Content-Type", "application/json");
			StringEntity requestEntity = new StringEntity(jsonPayload);
			httpPost.setEntity(requestEntity);
			logger.info("Request : " + " URL : " + httpPost + " payload : " + requestEntity + " request payload : " + jsonPayload);
			try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
				HttpEntity responseEntity = response.getEntity();
				int responseCode = response.getStatusLine().getStatusCode();

				if (responseCode == 200) {
					status = "SUCCESS";
					isCreated = true;
				} else {
					status = "FAILURE";
					isCreated = false;
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
			}
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Exception :" + str.toString());
		}
		return isCreated;
	}

	private Integer getskillSetId() {
		String maxVal;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(SkillsetQueryConstant.GET_SKILLSET_ID);
			maxVal = (String) queryObj.getSingleResult();
			if(maxVal==null || maxVal.isEmpty()) {
				maxVal="0";
			}
		} catch (Exception e) {
			logger.error("Error occured in SkillsetDaoImpl::getskillSetId" + e);
			return 1;
		}
		return Integer.valueOf(maxVal) + 1;
	}
	
	@Override
	public List<Object[]> getSkillsetDetail() {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(SkillsetQueryConstant.GET_SKILLSET_DET);
			resultList = queryObj.getResultList();
		} catch (Exception e) {
			logger.error("Error occured in SkillsetDaoImpl::getSkillsetDetail" + e);
			return resultList;
		}
		return resultList;
	}
	@Override
	public boolean updateSkillset(SkillsetRequest skillSetRequest) throws Exception {
		boolean isupdated;
		int insertVal;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(SkillsetQueryConstant.UPDATE_SKILLSET_DET);
			queryObj.setParameter("skillName", skillSetRequest.getSkillName());
			queryObj.setParameter("Language", skillSetRequest.getLanguage());
			queryObj.setParameter("TimeZone", skillSetRequest.getTimeZone());
			queryObj.setParameter("ChannelType", skillSetRequest.getChannelType());
			queryObj.setParameter("ServiceLevelThreshold", skillSetRequest.getServiceLevelThreshold());
			queryObj.setParameter("ServiceLevelGoal", skillSetRequest.getServiceLevelGoal());
			queryObj.setParameter("FirstCallResolution", skillSetRequest.getFirstCallResolution());
			queryObj.setParameter("AbandonedRateThreshold", skillSetRequest.getAbandonedRateThreshold());
			queryObj.setParameter("ShortCallThreshold", skillSetRequest.getShortCallThreshold());
			queryObj.setParameter("ShortAbandonedThreshold", skillSetRequest.getShortAbandonedThreshold());
			queryObj.setParameter("CountAbandonedSLA", skillSetRequest.getCountAbandonedSLA());
			queryObj.setParameter("skillsetId", skillSetRequest.getSkillsetId());
			queryObj.setParameter("Disposition", skillSetRequest.getDisposition());
			insertVal = queryObj.executeUpdate();
			if (insertVal > 0) {
					return true;
			}
		} catch (Exception e) {
			logger.error("Error occured in SkillsetDaoImpl::updateSkillset" + e);
			return false;
		}
		return false;
	}


}
