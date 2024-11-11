package com.example.authentication_uiux.models;

public class TermPolicyData {
    private String title;
    private String content;

    public TermPolicyData(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
