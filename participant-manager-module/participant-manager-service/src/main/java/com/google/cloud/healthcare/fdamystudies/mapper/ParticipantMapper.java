/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.mapper;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.ACTIVE_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.NOT_APPLICABLE;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.PENDING_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.SDF_DATE_TIME;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.STATUS_ACTIVE;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.STATUS_INACTIVE;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.STATUS_PENDING;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.YET_TO_ENROLL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.cloud.healthcare.fdamystudies.beans.EnrolledStudies;
import com.google.cloud.healthcare.fdamystudies.beans.Enrollments;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetails;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryDetail;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.Participants;
import com.google.cloud.healthcare.fdamystudies.common.CommonConstants;
import com.google.cloud.healthcare.fdamystudies.common.DateTimeUtils;
import com.google.cloud.healthcare.fdamystudies.common.OnboardingStatus;
import com.google.cloud.healthcare.fdamystudies.model.AppEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.SitePermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserDetailsEntity;

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
    return (participantRegistry.getOnboardingStatus().equals(OnboardingStatus.NEW.getCode())
        ? OnboardingStatus.NEW.getStatus()
        : OnboardingStatus.DISABLED.getStatus());
  }

  public static Enrollments toEnrollmentList(
      List<ParticipantStudyEntity> participantsEnrollments, List<String> participantStudyIds) {

    Enrollments enrollment = new Enrollments();
    for (ParticipantStudyEntity participantsEnrollment : participantsEnrollments) {
      participantStudyIds.add(participantsEnrollment.getStudy().getId());
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

  public static ParticipantRegistryDetail fromSite(
      SiteEntity site, SitePermissionEntity sitePermission, String siteId) {
    ParticipantRegistryDetail participants = new ParticipantRegistryDetail();
    participants.setSiteStatus(site.getStatus());

    if (site.getStudy() != null) {
      StudyEntity study = site.getStudy();
      participants.setStudyId(study.getId());
      participants.setStudyName(study.getName());
      participants.setCustomStudyId(study.getCustomId());
      participants.setSitePermission(sitePermission.getCanEdit());
      if (study.getAppInfo() != null) {
        participants.setAppName(study.getAppInfo().getAppName());
        participants.setCustomAppId(study.getAppInfo().getAppId());
        participants.setAppId(study.getAppInfo().getAppId());
      }
      if (site.getLocation() != null) {
        participants.setLocationName(site.getLocation().getName());
        participants.setCustomLocationId(site.getLocation().getCustomId());
        participants.setLocationStatus(site.getLocation().getStatus());
      }
    }
    participants.setSiteId(siteId);
    return participants;
  }

  public static Participants toParticipantDetails(
      UserDetailsEntity userDetailsEntity,
      Map<String, Map<StudyEntity, List<ParticipantStudyEntity>>>
          participantEnrollmentsByUserDetailsAndStudy,
      List<EnrolledStudies> enrolledStudies) {
    Participants participant = new Participants();
    participant.setId(userDetailsEntity.getId());
    participant.setEmail(userDetailsEntity.getEmail());

    // TODO(Monica) Integer is a wrapper class need to check for == or .equals()?
    if (userDetailsEntity.getStatus().equals(ACTIVE_STATUS)) {
      participant.setRegistrationStatus(STATUS_ACTIVE);
    } else if (userDetailsEntity.getStatus().equals(PENDING_STATUS)) {
      participant.setRegistrationStatus(STATUS_PENDING);
    } else {
      participant.setRegistrationStatus(STATUS_INACTIVE);
    }
    participant.setRegistrationDate(SDF_DATE_TIME.format(userDetailsEntity.getVerificationDate()));

    if (participantEnrollmentsByUserDetailsAndStudy.get(userDetailsEntity.getId()) != null) {
      Map<StudyEntity, List<ParticipantStudyEntity>> enrolledStudiesByStudyInfoId =
          participantEnrollmentsByUserDetailsAndStudy.get(userDetailsEntity.getId());
      EnrolledStudies enrolledStudy = StudyMapper.toEnrolledStudies(enrolledStudiesByStudyInfoId);
      enrolledStudies.add(enrolledStudy);
    }
    participant.setEnrolledStudies(enrolledStudies);
    return participant;
  }

  public static ParticipantRequest toParticipantDetails(
      List<ParticipantStudyEntity> participantStudies,
      ParticipantRegistrySiteEntity participantRegistrySite,
      ParticipantRequest participant) {

    participant.setId(participantRegistrySite.getId());
    participant.setEmail(participantRegistrySite.getEmail());
    String onboardingStatusCode = participantRegistrySite.getOnboardingStatus();
    participant.setOnboardingStatus(OnboardingStatus.fromCode(onboardingStatusCode).getStatus());
    Map<String, ParticipantStudyEntity> idMap = new HashMap<>();

    for (ParticipantStudyEntity participantStudy : participantStudies) {
      if (participantStudy.getParticipantRegistrySite() != null) {
        idMap.put(participantStudy.getParticipantRegistrySite().getId(), participantStudy);
      }
    }

    ParticipantStudyEntity participantStudy = idMap.get(participantRegistrySite.getId());
    if (participantStudy != null) {
      participant.setEnrollmentStatus(participantStudy.getStatus());
      String enrollmentDate = DateTimeUtils.format(participantStudy.getEnrolledDate());
      participant.setEnrollmentDate(StringUtils.defaultIfEmpty(enrollmentDate, NOT_APPLICABLE));
    } else {
      if (OnboardingStatus.NEW.getCode().equals(onboardingStatusCode)
          || OnboardingStatus.INVITED.getCode().equals(onboardingStatusCode)) {
        participant.setEnrollmentStatus(CommonConstants.YET_TO_ENROLL);
      }
    }

    String invitedDate = DateTimeUtils.format(participantRegistrySite.getInvitationDate());
    participant.setInvitedDate(StringUtils.defaultIfEmpty(invitedDate, NOT_APPLICABLE));
    return participant;
  }
}
