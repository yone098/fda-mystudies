/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.beans.AuditLogEventRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ImportParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetailRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetailsResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantStatusRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantStatusResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteDetailsResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SiteResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteStatusResponse;
import com.google.cloud.healthcare.fdamystudies.beans.UpdateTargetEnrollmentRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UpdateTargetEnrollmentResponse;
import org.springframework.web.multipart.MultipartFile;

public interface SiteService {

  public SiteResponse addSite(SiteRequest siteRequest, AuditLogEventRequest auditLogEventRequest);

  public InviteParticipantResponse inviteParticipants(
      InviteParticipantRequest inviteparticipantRequest, AuditLogEventRequest aleRequest);

  public SiteStatusResponse toggleSiteStatus(
      String userId, String siteId, AuditLogEventRequest auditLogEventRequest);

  public ParticipantResponse addNewParticipant(
      ParticipantDetailRequest participant, String userId, AuditLogEventRequest aleRequest);

  public SiteDetailsResponse getSites(String userId);

  public ParticipantDetailsResponse getParticipantDetails(
      String participantRegistrySiteId, String userId, AuditLogEventRequest aleRequest);

  public ParticipantRegistryResponse getParticipants(
      String userId, String siteId, String onboardingStatus);

  public ImportParticipantResponse importParticipants(
      String userId, String siteId, MultipartFile multipartFile, AuditLogEventRequest aleRequest);

  public ParticipantStatusResponse updateOnboardingStatus(
      ParticipantStatusRequest request, AuditLogEventRequest aleRequest);

  public UpdateTargetEnrollmentResponse updateTargetEnrollment(
      UpdateTargetEnrollmentRequest enrollmentRequest, AuditLogEventRequest aleRequest);
}
