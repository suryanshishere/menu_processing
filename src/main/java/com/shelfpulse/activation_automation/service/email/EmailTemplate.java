package com.shelfpulse.activation_automation.service.email;

public interface EmailTemplate {
    String getTemplateName();

    String render(Object data);
}
