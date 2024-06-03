package com.cognicx.AppointmentRemainder.response;

import com.cognicx.AppointmentRemainder.model.AuditFields;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

public class GenericAgentResponse<T> extends Response {
    private T data;
    private List<?> dataList;

    private int size;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<?> getDataList() {
        return dataList;
    }

    public void setDataList(List<?> dataList) {
        this.dataList = dataList;
    }
}
