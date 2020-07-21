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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.cloud.healthcare.fdamystudies.beans.EnrolledStudies;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryDetail;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.Participants;
import com.google.cloud.healthcare.fdamystudies.common.DateTimeUtils;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.common.OnboardingStatus;
import com.google.cloud.healthcare.fdamystudies.model.AppEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
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
}
