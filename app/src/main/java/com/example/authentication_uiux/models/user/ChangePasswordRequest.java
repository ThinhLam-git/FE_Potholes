package com.example.authentication_uiux.models.user;

public class ChangePasswordRequest {
    private String email;
    private String otp;
    private String newPassword;
    private String checkNewPassword;

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getCheckNewPassword() {
        return checkNewPassword;
    }

    public void setCheckNewPassword(String checkNewPassword) {
        this.checkNewPassword = checkNewPassword;
    }
}
