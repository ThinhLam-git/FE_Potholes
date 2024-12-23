package com.example.authentication_uiux.models.pothhole;

public class PotholeStatistics {
    private int totalPotholes;
    private int resolvedPotholes;
    private int reportedPotholes;
    private int inProgressPotholes;
    private double totalKilometers;

    public int getTotalPotholes() {
        return totalPotholes;
    }

    public void setTotalPotholes(int totalPotholes) {
        this.totalPotholes = totalPotholes;
    }

    public int getResolvedPotholes() {
        return resolvedPotholes;
    }

    public void setResolvedPotholes(int resolvedPotholes) {
        this.resolvedPotholes = resolvedPotholes;
    }

    public int getReportedPotholes() {
        return reportedPotholes;
    }

    public void setReportedPotholes(int reportedPotholes) {
        this.reportedPotholes = reportedPotholes;
    }

    public int getInProgressPotholes() {
        return inProgressPotholes;
    }

    public void setInProgressPotholes(int inProgressPotholes) {
        this.inProgressPotholes = inProgressPotholes;
    }

    public double getTotalKilometers() {
        return totalKilometers;
    }

    public void setTotalKilometers(double totalKilometers) {
        this.totalKilometers = totalKilometers;
    }
}