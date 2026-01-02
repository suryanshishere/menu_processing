package com.shelfpulse.activation_automation.service;

import com.shelfpulse.activation_automation.config.ApplicationProperties;
import com.shelfpulse.activation_automation.service.email.EmailTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MailService {

  private static final Logger log = LoggerFactory.getLogger(MailService.class);

  private final JavaMailSender javaMailSender;
  private final ApplicationProperties applicationProperties;
  private final Map<String, EmailTemplate> templateMap;

  public MailService(JavaMailSender javaMailSender,
      ApplicationProperties applicationProperties,
      List<EmailTemplate> templates) {
    this.javaMailSender = javaMailSender;
    this.applicationProperties = applicationProperties;
    this.templateMap = templates.stream()
        .collect(Collectors.toMap(EmailTemplate::getTemplateName, Function.identity()));
  }

  public static class Attachment {
    private String fileContent;
    private String fileFormat;
    private String name;

    // Getters and Setters
    public String getFileContent() {
      return fileContent;
    }

    public void setFileContent(String fileContent) {
      this.fileContent = fileContent;
    }

    public String getFileFormat() {
      return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
      this.fileFormat = fileFormat;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public void sendMail(String[] sendTo, String subject, String templateName, Object mailData,
      List<Attachment> attachments, String[] cc, String[] bcc) {
    try {
      EmailTemplate template = templateMap.get(templateName);
      if (template == null) {
        throw new IllegalArgumentException("Template not found: " + templateName);
      }

      String htmlContent = template.render(mailData);

      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

      helper.setFrom(applicationProperties.getEmail().getUser());
      helper.setTo(sendTo);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);

      if (cc != null && cc.length > 0) {
        helper.setCc(cc);
      }
      if (bcc != null && bcc.length > 0) {
        helper.setBcc(bcc);
      }

      if (attachments != null) {
        for (Attachment att : attachments) {
          String filename = att.getName() != null ? att.getName() : "attachment." + att.getFileFormat();

          byte[] contentBytes;
          try {
            contentBytes = Base64.getDecoder().decode(att.getFileContent());
          } catch (IllegalArgumentException e) {
            contentBytes = att.getFileContent().getBytes(StandardCharsets.UTF_8);
          }

          helper.addAttachment(filename, new ByteArrayResource(contentBytes));
        }
      }

      javaMailSender.send(message);
      log.info("Email sent to: {}", (Object) sendTo);

    } catch (MessagingException e) {
      log.error("Error sending email: ", e);
      throw new RuntimeException("Error sending email: " + e.getMessage());
    }
  }

  public void sendMail(String sendTo, String subject, String templateName, Object mailData,
      List<Attachment> attachments, String cc, String[] bcc) {
    sendMail(new String[] { sendTo }, subject, templateName, mailData, attachments,
        cc != null ? new String[] { cc } : null, bcc);
  }
}
