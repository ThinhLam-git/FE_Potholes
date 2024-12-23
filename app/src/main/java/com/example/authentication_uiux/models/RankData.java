package com.example.authentication_uiux.models;

public class RankData {
    private String name;
    private int avatarResource;
    private int potholesDetected;
    private double kilometersTraveled;

    public RankData(String name, int avatarResource, int potholesDetected, double kilometersTraveled) {
        this.name = name;
        this.avatarResource = avatarResource;
        this.potholesDetected = potholesDetected;
        this.kilometersTraveled = kilometersTraveled;
    }

    public String getName() {
        return name;
    }

    public int getAvatarResource() {
        return avatarResource;
    }

    public int getPotholesDetected() {
        return potholesDetected;
    }

    public double getKilometersTraveled() {
        return kilometersTraveled;
    }

    public int getScore() {
        return potholesDetected + (int) kilometersTraveled;
    }
}