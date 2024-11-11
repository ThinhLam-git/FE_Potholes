package com.example.authentication_uiux.models;

public class RankData {
    private String name;
    private int avatarResource;
    private int score;

    public RankData(String name, int avatarResource, int score) {
        this.name = name;
        this.avatarResource = avatarResource;
        this.score = score;
    }

    // Getters
    public String getName() { return name; }
    public int getAvatarResource() { return avatarResource; }
    public int getScore() { return score; }
}