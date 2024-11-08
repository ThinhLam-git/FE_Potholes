package com.example.authentication_uiux;

public class RankItem {
    private String name;
    private int avatarResource;
    private int score;

    public RankItem(String name, int avatarResource, int score) {
        this.name = name;
        this.avatarResource = avatarResource;
        this.score = score;
    }

    // Getters
    public String getName() { return name; }
    public int getAvatarResource() { return avatarResource; }
    public int getScore() { return score; }
}