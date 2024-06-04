package com.cognicx.AppointmentRemainder.ami;

import org.asteriskjava.manager.event.QueueMemberEvent;

public class QueueMemberStatusEvent extends QueueMemberEvent {
    private Integer status;

	public QueueMemberStatusEvent(String queue, String memberName, Integer status) {
        super(queue); // Assuming QueueMemberEvent constructor accepts queue and memberName
        this.status = status;
    }
}

	
