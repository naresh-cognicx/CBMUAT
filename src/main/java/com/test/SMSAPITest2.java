package com.test;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class SMSAPITest2 {


	public void invokeAPI() {
		try {

			String apiUrl = "https://avayaapi.ezdanholding.qa/api/sms/SendSmsAsync?ApiKey=AVAyA-87jkl)-(&bnj8E8Z5D5A_N2]";

			// Define the JSON payload to send in the request body
			String phoneNumber="12345887";
			String messageContent="UAT";
			String jsonPayload = "{ \"phoneNumber\": \"" + phoneNumber + "\", \"messageContent\": \"" + messageContent + "\" }";

			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				// Create an instance of HttpPost with the API URL
				HttpPost httpPost = new HttpPost(apiUrl);
				
				httpPost.setHeader("Content-Type", "application/json");
				httpPost.setHeader("ApiKey", "AVAyA-87jkl)-(&bnj8E8Z5D5A_N2]");

				// Set the request body as a JSON string entity
				StringEntity requestEntity = new StringEntity(jsonPayload);
				httpPost.setEntity(requestEntity);

				// Execute the request and obtain the response
				try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
					// Retrieve the response entity
					HttpEntity responseEntity = response.getEntity();

					// Print the response status code
					System.out.println("Response Status Code: " + response.getStatusLine().getStatusCode());

					// Print the response body, if available
					if (responseEntity != null) {
						String responseBody = EntityUtils.toString(responseEntity);
						System.out.println("Response Body: " + responseBody);
					}

				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		SMSAPITest2 test2=new SMSAPITest2();
		test2.invokeAPI();
	}
}
