/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.beans.EmailRequest;
import com.google.cloud.healthcare.fdamystudies.beans.EmailResponse;
import com.google.cloud.healthcare.fdamystudies.config.ApplicationPropertyConfiguration;
import com.google.cloud.healthcare.fdamystudies.util.EmailNotification;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserSupportServiceImpl implements UserSupportService {

  private static final Logger logger = LoggerFactory.getLogger(UserSupportServiceImpl.class);

  @Autowired ApplicationPropertyConfiguration appConfig;

  @Autowired EmailNotification emailNotification;

  @Autowired private EmailService emailService;

  @Override
  public EmailResponse feedback(String subject, String body) {
    logger.info("UserManagementProfileServiceImpl - feedback() :: Starts");
    String feedbackSubject = appConfig.getFeedbackMailSubject() + subject;
    String feedbackBody = appConfig.getFeedbackMailBody();
    Map<String, String> templateArgs = new HashMap<>();
    templateArgs.put("body", body);
    EmailRequest emailRequest =
        new EmailRequest(
            appConfig.getFromEmailAddress(),
            new String[] {appConfig.getFeedbackToEmail()},
            null,
            null,
            feedbackSubject,
            feedbackBody,
            templateArgs);
    return emailService.sendMimeMail(emailRequest);
  }

  @Override
  public EmailResponse contactUsDetails(
      String subject, String body, String firstName, String email) {
    logger.info("AppMetaDataOrchestration - contactUsDetails() :: Starts");
    String contactUsSubject = appConfig.getContactusMailSubject() + subject;
    String contactUsContent = appConfig.getContactusMailBody();
    Map<String, String> templateArgs = new HashMap<>();
    templateArgs.put("firstName", firstName);
    templateArgs.put("email", email);
    templateArgs.put("subject", subject);
    templateArgs.put("body", body);

    EmailRequest emailRequest =
        new EmailRequest(
            appConfig.getFromEmailAddress(),
            new String[] {appConfig.getContactusToEmail()},
            null,
            null,
            contactUsSubject,
            contactUsContent,
            templateArgs);
    return emailService.sendMimeMail(emailRequest);
  }
}
