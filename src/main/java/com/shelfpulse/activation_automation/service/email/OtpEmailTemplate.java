package com.shelfpulse.activation_automation.service.email;

import com.shelfpulse.activation_automation.dto.email.OtpMailData;
import com.shelfpulse.activation_automation.enums.MailType;
import org.springframework.stereotype.Component;

@Component
public class OtpEmailTemplate implements EmailTemplate {

    @Override
    public String getTemplateName() {
        return "otpEmailTemplate";
    }

    @Override
    public String render(Object data) {
        if (!(data instanceof OtpMailData)) {
            throw new IllegalArgumentException("Invalid data for OtpEmailTemplate. Expected OtpMailData.");
        }
        OtpMailData mailData = (OtpMailData) data;
        return generateHtml(mailData);
    }

    private String generateHtml(OtpMailData mailData) {
        boolean isPasswordReset = mailData.getType() == MailType.RESET_PASSWORD;
        String subject = isPasswordReset ? "Password Reset Request" : "Profile Update Verification";
        String actionText = isPasswordReset ? "reset your password" : "update your profile";
        String userName = mailData.getUserName() != null ? " " + mailData.getUserName() : "";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>%s</title>
                  <style>
                    body {
                      font-family: 'Helvetica Neue', Arial, sans-serif;
                      line-height: 1.6;
                      color: #333333;
                      margin: 0;
                      padding: 0;
                      background-color: #f4f4f4;
                    }
                    .container {
                      max-width: 640px;
                      margin: 40px auto;
                      padding: 30px;
                      border-radius: 8px;
                      background-color: #ffffff;
                      box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                    }
                    .header {
                      text-align: center;
                      padding-bottom: 25px;
                      border-bottom: 1px solid #e5e5e5;
                    }
                    .header h2 {
                      color: #2c3e50;
                      margin: 0;
                      font-size: 24px;
                    }
                    .content {
                      padding: 20px 0;
                    }
                    .greeting {
                      font-size: 16px;
                      margin-bottom: 20px;
                    }
                    .otp-container {
                      background-color: #f8f9fa;
                      padding: 15px;
                      border-radius: 5px;
                      text-align: center;
                      margin: 20px 0;
                    }
                    .otp-code {
                      font-size: 28px;
                      font-weight: 700;
                      color: #e74c3c;
                      letter-spacing: 2px;
                      margin: 10px 0;
                    }
                    .instructions {
                      color: #666666;
                      font-size: 14px;
                    }
                    .footer {
                      margin-top: 25px;
                      padding-top: 20px;
                      border-top: 1px solid #e5e5e5;
                      text-align: center;
                      font-size: 12px;
                      color: #888888;
                    }
                    .company-name {
                      font-weight: bold;
                      color: #2c3e50;
                    }
                    .warning {
                      color: #7f8c8d;
                      font-size: 13px;
                      margin-top: 15px;
                    }
                  </style>
                </head>
                <body>
                  <div class="container">
                    <div class="header">
                      <h2>%s</h2>
                    </div>
                    <div class="content">
                      <p class="greeting">
                        Hello%s,
                      </p>
                      <p>
                        We received a request to %s. Please use the one-time password (OTP) below to proceed:
                      </p>
                      <div class="otp-container">
                        <div class="otp-code">%s</div>
                        <div class="instructions">
                          Please use this OTP to %s within %d minutes
                        </div>
                      </div>
                      <p class="warning">
                        If you did not initiate this request, please ignore this email or contact our support team immediately.
                      </p>
                    </div>
                    <div class="footer">
                      <p>Need help? Contact our support team at <a href="mailto:support@shelfexecution.com">support@shelfexecution.com</a></p>
                      <p>&copy; 2025 <span class="company-name">ShelfEx LLC</span>. All rights reserved.</p>
                    </div>
                  </div>
                </body>
                </html>
                """
                .formatted(subject, subject, userName, actionText, mailData.getOtp(), actionText,
                        mailData.getExpiryIn());
    }
}
