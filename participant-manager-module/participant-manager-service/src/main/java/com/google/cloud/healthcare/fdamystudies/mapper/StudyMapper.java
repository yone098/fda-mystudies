/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.mapper;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.SDF_DATE_TIME;

import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetail;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryDetail;
import com.google.cloud.healthcare.fdamystudies.model.AppEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;

public final class StudyMapper {

  private StudyMapper() {}

  public static ParticipantDetail fromParticipantStudy(ParticipantStudyEntity participantStudy) {
    ParticipantDetail participantDetail = new ParticipantDetail();
    participantDetail.setId(participantStudy.getParticipantId());
    participantDetail.setEnrollmentStatus(participantStudy.getStatus());
    participantDetail.setEmail(participantStudy.getParticipantRegistrySite().getEmail());
    participantDetail.setSiteId(participantStudy.getSite().getId());
    participantDetail.setCustomLocationId(participantStudy.getSite().getLocation().getCustomId());
    participantDetail.setLocationName(participantStudy.getSite().getLocation().getName());

    participantDetail.setInvitedDate(
        participantStudy.getParticipantRegistrySite().getInvitationDate() != null
            ? SDF_DATE_TIME.format(
                participantStudy.getParticipantRegistrySite().getInvitationDate())
            : "NA");
    participantDetail.setEnrollmentDate(
        participantStudy.getEnrolledDate() != null
            ? SDF_DATE_TIME.format(participantStudy.getEnrolledDate())
            : "NA");
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
}
