/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.healthcare.fdamystudies.beans.DecomissionSiteResponse;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetailResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteDetails;
import com.google.cloud.healthcare.fdamystudies.beans.SiteRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SiteResponse;

public interface SiteService {

  public SiteResponse addSite(SiteRequest siteRequest);

  public InviteParticipantResponse inviteParticipants(
      InviteParticipantRequest inviteparticipantBean);

  public DecomissionSiteResponse decomissionSite(String userId, String siteId);

  public ParticipantResponse addNewParticipant(ParticipantRequest participant, String userId);

  public SiteDetails getSites(String userId);

  public ParticipantDetailResponse getParticipantDetails(
      String participantRegistrySiteId, String userId);

  public ParticipantRegistryResponse getParticipants(
      String userId, String siteId, String onboardingStatus);

  public ParticipantRegistryResponse importParticipant(
      String userId, String siteId, MultipartFile multipartFile);
}
