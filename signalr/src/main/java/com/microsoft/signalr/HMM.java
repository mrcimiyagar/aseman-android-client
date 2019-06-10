package com.microsoft.signalr;

import java.util.List;

public class HMM {

    private int type;
    private String invocationId;
    private String target;
    private String error;
    private List<Object> arguments;
    private Object result;

    public int getType() {
        return type;
    }

    public HubMessageType getHubMessageType() {
        return HubMessageType.values()[type - 1];
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getInvocationId() {
        return invocationId;
    }

    public void setInvocationId(String invocationId) {
        this.invocationId = invocationId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
