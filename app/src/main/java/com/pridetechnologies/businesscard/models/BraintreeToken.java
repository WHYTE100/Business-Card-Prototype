package com.pridetechnologies.businesscard.models;

public class BraintreeToken {
    private String token;
    private boolean success;
    private boolean error;

    public BraintreeToken()
    {

    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
