/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.controller;

import static com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent.NEW_USER_INVITATION_EMAIL_SENT;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import com.google.cloud.healthcare.fdamystudies.beans.AuditLogEventRequest;
import com.google.cloud.healthcare.fdamystudies.common.BaseMockIT;
import com.google.cloud.healthcare.fdamystudies.common.IdGenerator;
import com.google.cloud.healthcare.fdamystudies.helper.TestDataHelper;
import com.google.cloud.healthcare.fdamystudies.model.SendAdminInvitationEmailEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.AddNewAdminEmailServiceRepository;
import com.google.cloud.healthcare.fdamystudies.service.ManageUserService;
import java.util.List;
import java.util.Map;
import javax.mail.internet.MimeMessage;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class InviteAdminsTaskScheduledTest extends BaseMockIT {

  @Autowired private TestDataHelper testDataHelper;

  @Autowired private ManageUserService manageUserService;

  @Autowired private AddNewAdminEmailServiceRepository addNewAdminEmailServiceRepository;

  @BeforeEach
  public void setUp() {}

  @Test
  public void shouldSendEmailInvitation() throws Exception {

    UserRegAdminEntity admin = testDataHelper.createNonSuperAdmin();
    admin.setSecurityCode(IdGenerator.id());
    testDataHelper.getUserRegAdminRepository().saveAndFlush(admin);

    SendAdminInvitationEmailEntity adminRecordToSendEmail = new SendAdminInvitationEmailEntity();
    adminRecordToSendEmail.setUserId(admin.getId());
    addNewAdminEmailServiceRepository.saveAndFlush(adminRecordToSendEmail);

    manageUserService.sendInvitationEmailForNewAdmins();

    verify(emailSender, atLeastOnce()).send(isA(MimeMessage.class));

    List<SendAdminInvitationEmailEntity> invitedAdmins =
        addNewAdminEmailServiceRepository.findAll();

    assertTrue(invitedAdmins.isEmpty());

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    //    auditRequest.setUserId(admin.getId());
    Map<String, AuditLogEventRequest> auditEventMap = new HashedMap<>();
    auditEventMap.put(NEW_USER_INVITATION_EMAIL_SENT.getEventCode(), auditRequest);

    verifyAuditEventCall(auditEventMap, NEW_USER_INVITATION_EMAIL_SENT);
  }

  @AfterEach
  public void clean() {
    testDataHelper.cleanUp();
  }
}
