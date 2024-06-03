package com.cognicx.AppointmentRemainder.Request;

import lombok.Data;

@Data
public class PageRequest {
    private int limit;
    private int offset;
}
