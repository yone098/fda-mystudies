/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.beans.ConsentDocument;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.healthcare.fdamystudies.beans.EnableDisableParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.EnableDisableParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ImportParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetailRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetailsResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteDetailsResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SiteResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteStatusResponse;
import com.google.cloud.healthcare.fdamystudies.beans.UpdateTargetEnrollmentRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UpdateTargetEnrollmentResponse;

public interface SiteService {

  public SiteResponse addSite(SiteRequest siteRequest);

  public InviteParticipantResponse inviteParticipants(
      InviteParticipantRequest inviteparticipantBean);

  public SiteStatusResponse toggleSiteStatus(String userId, String siteId);

  public ParticipantResponse addNewParticipant(ParticipantDetailRequest participant, String userId);

  public SiteDetailsResponse getSites(String userId);

  public ParticipantDetailsResponse getParticipantDetails(
      String participantRegistrySiteId, String userId);

  public ParticipantRegistryResponse getParticipants(
      String userId, String siteId, String onboardingStatus);

  public ImportParticipantResponse importParticipant(
      String userId, String siteId, MultipartFile multipartFile);

  ConsentDocument getConsentDocument(String consentId, String userId);

  public EnableDisableParticipantResponse updateOnboardingStatus(
      EnableDisableParticipantRequest request);

  public UpdateTargetEnrollmentResponse updateTargetEnrollment(
      UpdateTargetEnrollmentRequest enrollmentRequest);
}
