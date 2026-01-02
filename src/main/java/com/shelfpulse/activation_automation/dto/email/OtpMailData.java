package com.shelfpulse.activation_automation.dto.email;

import com.shelfpulse.activation_automation.enums.MailType;

public class OtpMailData {
    private MailType type;
    private String otp;
    private int expiryIn;
    private String userName;

    public OtpMailData() {
    }

    public OtpMailData(MailType type, String otp, int expiryIn, String userName) {
        this.type = type;
        this.otp = otp;
        this.expiryIn = expiryIn;
        this.userName = userName;
    }

    public MailType getType() {
        return type;
    }

    public void setType(MailType type) {
        this.type = type;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public int getExpiryIn() {
        return expiryIn;
    }

    public void setExpiryIn(int expiryIn) {
        this.expiryIn = expiryIn;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
