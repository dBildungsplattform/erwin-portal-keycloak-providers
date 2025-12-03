package com.spsh.oidc.dto;

public class FetchUrlResponse {
    private String mapToLmsRolle;
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMapToLmsRolle() {
        return mapToLmsRolle;
    }

    public void setMapToLmsRolle(String mapToLmsRolle) {
        this.mapToLmsRolle = mapToLmsRolle;
    }
}
