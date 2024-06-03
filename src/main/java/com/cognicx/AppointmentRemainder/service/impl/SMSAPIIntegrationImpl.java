package com.cognicx.AppointmentRemainder.service.impl;

import java.io.PrintWriter;
import java.io.StringWriter;

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
import org.springframework.stereotype.Service;

import com.cognicx.AppointmentRemainder.service.SMSAPIIntegration;

@Service
public class SMSAPIIntegrationImpl implements SMSAPIIntegration {

	private Logger logger = LoggerFactory.getLogger(SMSAPIIntegrationImpl.class);

	@Value("${call.apiurl.sms.url}")
	private String smsURL;

	@Value("${call.apiurl.sms.en_messagecontent}")
	private String en_messageContent;


	@Value("${call.apiurl.sms.ar_messagecontent}")
	private String ar_messageContent;

	@Value("${call.apiurl.sms.apiKey}")
	private String apiKey;

	@Value("${call.apiurl.sms.english_language}")
	private String englishlanguage;


	@Override
	public String sendSMS(String phoneNumber,String language,String actionID) {

		String status="FAILURE";
		String messageContent=null;
		try {

			if(language!=null && language.equalsIgnoreCase(englishlanguage)) {
				messageContent=en_messageContent+"&"+"actionID"+"="+actionID+"&"+"phoneNumber"+"="+phoneNumber;
			}else {
				messageContent=ar_messageContent+"&"+"actionID"+"="+actionID+"&"+"phoneNumber"+"="+phoneNumber;
			}

			String jsonPayload = "{ \"phoneNumber\": \"" + phoneNumber + "\", \"messageContent\": \"" + messageContent + "\" }";

			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				HttpPost httpPost = new HttpPost(smsURL);
				httpPost.setHeader("Content-Type", "application/json");
				httpPost.setHeader("ApiKey", apiKey);


				httpPost.setHeader("ActionId",actionID);
				httpPost.setHeader("mobile",phoneNumber);
				StringEntity requestEntity = new StringEntity(jsonPayload);
				httpPost.setEntity(requestEntity);
				logger.info("Request : "+ " URL : "+httpPost+ " payload : "+requestEntity+" request payload : "+jsonPayload);
				try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
					HttpEntity responseEntity = response.getEntity();
					int responseCode=response.getStatusLine().getStatusCode();

					if(responseCode==200) {
						status="SUCCESS";
					}else {
						status="FAILURE";
					}
					logger.info("Response Status Code: " + response.getStatusLine().getStatusCode());
					if (responseEntity != null) {
						String responseBody = EntityUtils.toString(responseEntity);
						logger.info(responseBody);
					}
				}catch(Exception e) {
					StringWriter str=new StringWriter();
					e.printStackTrace(new PrintWriter(str));
					logger.error("Exception :"+str.toString());
				}

			}catch(Exception e) {
				StringWriter str=new StringWriter();
				e.printStackTrace(new PrintWriter(str));
				logger.error("Exception :"+str.toString());
			}

		}catch(Exception e) {
			StringWriter str=new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Exception :"+str.toString());
		}
		return status;

	}
}
