package com.cognicx.AppointmentRemainder.ami;

import org.asteriskjava.live.AsteriskAgent;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.AsteriskQueueEntry;
import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.AsteriskServerListener;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.MeetMeUser;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.QueueMemberEvent;
import org.asteriskjava.manager.event.QueueMemberStatusEvent;
import org.asteriskjava.manager.event.UserEvent;
import org.asteriskjava.manager.action.StatusAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cognicx.AppointmentRemainder.service.AsteriskService;
import com.cognicx.AppointmentRemainder.service.impl.CampaignServiceImpl;

@Configuration
public class AsteriskConn implements ManagerEventListener,AsteriskServerListener {

	@Value("${asterisk.manager.host}")
	private String host;

	@Value("${asterisk.manager.port}")
	private int port;

	@Value("${asterisk.manager.username}")
	private String username;

	@Value("${asterisk.manager.password}")
	private String password;

	
	@Autowired
	AsteriskService asteriskService;

	private static final Logger logger = LoggerFactory.getLogger(AsteriskConn.class);


	@Bean
	public ManagerConnection asteriskManagerConnection() {
		AsteriskServer asteriskServer = new DefaultAsteriskServer(host,port,username,password);
		ManagerConnection managerConnection=asteriskServer.getManagerConnection();
		try {
			managerConnection.login();
			managerConnection.addEventListener(this);
			asteriskServer.addAsteriskServerListener(this);
			managerConnection.registerUserEventClass(calldialEvent.class);
			managerConnection.registerUserEventClass(callhangupEvent.class);
			managerConnection.registerUserEventClass(callconnecedEvent.class);
			logger.error("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return managerConnection;
	}


	/*@Override
	public void onManagerEvent(ManagerEvent event) {
		if(event instanceof UserEvent) {
			logger.info("Event :"+event.toString());
			UserEvent userEvent=(UserEvent) event;
			if(event instanceof calldialEvent) {
				calldialEvent callDial=(calldialEvent) event;
				logger.info("Dial Event Invoked");
				//asteriskService.insertDialContDetails(userEvent);
			}else if(event instanceof callconnecedEvent) {
				callconnecedEvent callConn=(callconnecedEvent) event;
				asteriskService.insertActiveContDetails(userEvent);
			}else if(event instanceof callhangupEvent) {
				callhangupEvent callhang=(callhangupEvent) event;
				asteriskService.updateActiveContDetails(userEvent);
			}else if(event instanceof QueueMemberEvent) {
				logger.info("QueueMemberEvent Invoked");
				QueueMemberEvent queueMemEvent=(QueueMemberEvent) event;
				asteriskService.updateQueueMemEvent(queueMemEvent);
			}else if(event instanceof QueueMemberStatusEvent) {
				logger.info("QueueMember Status Event Invoked");
			//	asteriskService.handleQueueMemberStatusEvent((QueueMemberStatusEvent) event);
				//QueueMemberStatusEvent queStatusEvent=(QueueMemberStatusEvent) event;
				QueueMemberStatusEvent queueMemberStatusEvent = (QueueMemberStatusEvent) event;
	            // Handle QueueMemberStatusEvent
	            asteriskService.handleQueueMemberStatusEvent(queueMemberStatusEvent);
			}
		}
	}*/

	@Override
	public void onManagerEvent(ManagerEvent event) {
	    logger.info("Received event: {}", event);
	    if (event instanceof QueueMemberStatusEvent) {
	        logger.info("Received QueueMemberStatusEvent");
	        QueueMemberStatusEvent queueMemberStatusEvent = (QueueMemberStatusEvent) event;
	        asteriskService.handleQueueMemberStatusEvent(queueMemberStatusEvent);
	    }
	}


	@Override
	public void onNewAsteriskChannel(AsteriskChannel channel) {

	}

	@Override
	public void onNewMeetMeUser(MeetMeUser user) {

	}


	@Override
	public void onNewAgent(AsteriskAgent agent) {

	}

	@Override
	public void onNewQueueEntry(AsteriskQueueEntry entry) {
	}

}
