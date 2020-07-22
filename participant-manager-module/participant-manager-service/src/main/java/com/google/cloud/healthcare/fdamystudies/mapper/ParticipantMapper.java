/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.mapper;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.NOT_APPLICABLE;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.YET_TO_ENROLL;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.cloud.healthcare.fdamystudies.beans.Enrollments;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetails;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryDetail;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.common.DateTimeUtils;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.common.OnboardingStatus;
import com.google.cloud.healthcare.fdamystudies.model.AppEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;

public final class ParticipantMapper {

  private ParticipantMapper() {}

  public static ParticipantRequest fromParticipantStudy(ParticipantStudyEntity participantStudy) {
    ParticipantRequest participantDetail = new ParticipantRequest();
    participantDetail.setId(participantStudy.getParticipantId());
    participantDetail.setEnrollmentStatus(participantStudy.getStatus());
    participantDetail.setEmail(participantStudy.getParticipantRegistrySite().getEmail());
    participantDetail.setSiteId(participantStudy.getSite().getId());
    participantDetail.setCustomLocationId(participantStudy.getSite().getLocation().getCustomId());
    participantDetail.setLocationName(participantStudy.getSite().getLocation().getName());

    String invitedDate =
        DateTimeUtils.format(participantStudy.getParticipantRegistrySite().getInvitationDate());
    participantDetail.setInvitedDate(StringUtils.defaultIfEmpty(invitedDate, NOT_APPLICABLE));

    String enrollmentDate = DateTimeUtils.format(participantStudy.getEnrolledDate());
    participantDetail.setEnrollmentDate(StringUtils.defaultIfEmpty(enrollmentDate, NOT_APPLICABLE));
    return participantDetail;
  }

  public static ParticipantRegistryDetail fromStudyAndApp(StudyEntity study, AppEntity app) {
    ParticipantRegistryDetail participantRegistryDetail = new ParticipantRegistryDetail();
    participantRegistryDetail.setStudyId(study.getId());
    participantRegistryDetail.setCustomStudyId(study.getCustomId());
    participantRegistryDetail.setStudyName(study.getName());
    participantRegistryDetail.setStudyType(study.getType());
    participantRegistryDetail.setAppId(app.getId());
    participantRegistryDetail.setAppName(app.getAppName());
    participantRegistryDetail.setCustomAppId(app.getAppId());
    return participantRegistryDetail;
  }

  public static ParticipantResponse toParticipantResponse(
      ParticipantRegistrySiteEntity participantRegistrySite) {
    ParticipantResponse response = new ParticipantResponse(MessageCode.ADD_PARTICIPANT_SUCCESS);
    response.setParticipantId(participantRegistrySite.getId());
    return response;
  }

  public static ParticipantRegistrySiteEntity fromParticipantRequest(
      ParticipantRequest participantRequest, SiteEntity site) {
    ParticipantRegistrySiteEntity participantRegistrySite = new ParticipantRegistrySiteEntity();
    participantRegistrySite.setEmail(participantRequest.getEmail());
    participantRegistrySite.setSite(site);
    participantRegistrySite.setOnboardingStatus(OnboardingStatus.NEW.getCode());
    participantRegistrySite.setEnrollmentToken(RandomStringUtils.randomAlphanumeric(8));
    participantRegistrySite.setStudy(site.getStudy());
    return participantRegistrySite;
  }

  public static ParticipantDetails toParticipantDetailsResponse(
      ParticipantRegistrySiteEntity participantRegistry) {

    ParticipantDetails participantDetails = new ParticipantDetails();

    participantDetails.setAppName(participantRegistry.getStudy().getAppInfo().getAppName());
    participantDetails.setCustomAppId(participantRegistry.getStudy().getAppInfo().getAppId());
    participantDetails.setStudyName(participantRegistry.getStudy().getName());
    participantDetails.setCustomStudyId(participantRegistry.getStudy().getCustomId());
    participantDetails.setLocationName(participantRegistry.getSite().getLocation().getName());
    participantDetails.setCustomLocationId(
        participantRegistry.getSite().getLocation().getCustomId());
    participantDetails.setEmail(participantRegistry.getEmail());

    String invitedDate = DateTimeUtils.format(participantRegistry.getInvitationDate());
    participantDetails.setInvitationDate(StringUtils.defaultIfEmpty(invitedDate, NOT_APPLICABLE));

    participantDetails.setOnboardringStatus(getOnboardingStatus(participantRegistry));

    participantDetails.setParticipantRegistrySiteid(participantRegistry.getId());

    return participantDetails;
  }

  private static String getOnboardingStatus(ParticipantRegistrySiteEntity participantRegistry) {
    if (participantRegistry
        .getOnboardingStatus()
        .equalsIgnoreCase(OnboardingStatus.INVITED.getCode())) {
      return OnboardingStatus.INVITED.getStatus();
    }
    return (participantRegistry.getOnboardingStatus().equals("N")
        ? OnboardingStatus.NEW.getStatus()
        : OnboardingStatus.DISABLED.getStatus());
  }

  public static Enrollments toEnrollmentList(
      List<ParticipantStudyEntity> participantsEnrollments, List<String> participantStudyIds) {

    Enrollments enrollment = new Enrollments();
    for (ParticipantStudyEntity participantsEnrollment : participantsEnrollments) {
      participantStudyIds.add(participantsEnrollment.getParticipantId());
      enrollment.setEnrollmentStatus(participantsEnrollment.getStatus());
      enrollment.setParticipantId(participantsEnrollment.getParticipantId());

      String enrollmentDate = DateTimeUtils.format(participantsEnrollment.getEnrolledDate());
      enrollment.setEnrollmentDate(StringUtils.defaultIfEmpty(enrollmentDate, NOT_APPLICABLE));

      String withdrawalDate = DateTimeUtils.format(participantsEnrollment.getWithdrawalDate());
      enrollment.setWithdrawalDate(StringUtils.defaultIfEmpty(withdrawalDate, NOT_APPLICABLE));
    }
    return enrollment;
  }

  public static Enrollments toEnrollments() {
    Enrollments enrollment = new Enrollments();
    enrollment.setEnrollmentStatus(YET_TO_ENROLL);
    enrollment.setEnrollmentDate("-");
    enrollment.setWithdrawalDate("-");
    return enrollment;
  }
}
