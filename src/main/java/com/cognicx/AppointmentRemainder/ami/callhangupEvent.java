package com.cognicx.AppointmentRemainder.ami;

import org.asteriskjava.manager.event.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class callhangupEvent extends UserEvent{


	private static Logger logger = LoggerFactory.getLogger(callhangupEvent.class);

	private String calluid;



	private String productid;
	private String phone;
	private String appdata;
	private String hangupcause;
	private String campaingnname;

	public String getHangupcause() {
		return hangupcause;
	}

	public void setHangupcause(String hangupcause) {
		this.hangupcause = hangupcause;
	}


	public String getPhone() {
		logger.info("Getting Phone number in Call Hang UP "+phone);
		return phone;
	}

	public void setPhone(String phone) {
		logger.info("setting Phone Number in Call Hang UP"+phone);
		this.phone = phone;
	}

	public callhangupEvent(Object source) {
		super(source);
	}

	public String getCalluid() {
		logger.info("Getting Call ID"+calluid);
		return calluid;
	}

	public void setCalluid(String calluid) {
		logger.info("Setting Call ID");
		this.calluid = calluid;
	}

	public String getAppdata() {
		logger.info("getting App Data in Call Hang UP"+appdata);
		return appdata;
	}

	public void setAppdata(String appdata) {
		logger.info("Setting App Data in Call Hang UP "+appdata);
		this.appdata = appdata;
	}


	public String getCampaingnname() {
		return campaingnname;
	}

	public void setCampaingnname(String campaingnname) {
		this.campaingnname = campaingnname;
	}


	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public String getProductid() {
		return productid;
	}

	public void setProductid(String productid) {
		this.productid = productid;
	}


}
