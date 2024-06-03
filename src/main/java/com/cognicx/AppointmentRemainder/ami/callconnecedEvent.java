package com.cognicx.AppointmentRemainder.ami;

import org.asteriskjava.manager.event.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class callconnecedEvent extends UserEvent{

	private static Logger logger = LoggerFactory.getLogger(callconnecedEvent.class);

	private String calluid;
	private String productid;
	private String phone;
	private String appdata;
	private String campaingnname;

	public String getProductid() {
		logger.info("Getting Product ID in Call connect"+productid);
		return productid;
	}

	public void setProductid(String productid) {
		logger.info("setting Product ID in Call connect "+productid);
		this.productid = productid;
	}

	public String getPhone() {
		logger.info("Getting Phone number in Call connect "+phone);
		return phone;
	}

	public void setPhone(String phone) {
		logger.info("setting Phone Number in Call connect"+phone);
		this.phone = phone;
	}

	public callconnecedEvent(Object source) {
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
		logger.info("getting App Data in Call connect"+appdata);
		return appdata;
	}

	public void setAppdata(String appdata) {
		logger.info("Setting App Data in Call connect "+appdata);
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



}
