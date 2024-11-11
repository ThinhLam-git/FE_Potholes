package com.example.authentication_uiux.models;


public class PotholeData {
    private double latitude;
    private double longitude;
    private String detectionTime;
    private String user;
    private String status;

    public PotholeData(double latitude, double longitude, String detectionTime, String user, String status) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.detectionTime = detectionTime;
        this.user = user;
        this.status = status;
    }

    // Getters and Setters

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetectionTime() {
        return detectionTime;
    }

    public void setDetectionTime(String detectionTime) {
        this.detectionTime = detectionTime;
    }
}

