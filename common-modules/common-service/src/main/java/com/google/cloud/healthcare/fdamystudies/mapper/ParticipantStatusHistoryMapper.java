/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.mapper;

import com.google.cloud.healthcare.fdamystudies.common.EnrollmentStatus;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStatusHistoryEntity;

public class ParticipantStatusHistoryMapper {

  public static ParticipantStatusHistoryEntity toParticipantStatusHistoryEntity(
      ParticipantRegistrySiteEntity participantRegistrySiteEntity, EnrollmentStatus enrollment) {
    ParticipantStatusHistoryEntity participantStatusEntity = new ParticipantStatusHistoryEntity();
    participantStatusEntity.setStatus(enrollment.getStatus());
    participantStatusEntity.setParticipantRegistrySite(participantRegistrySiteEntity);
    participantStatusEntity.setSite(participantRegistrySiteEntity.getSite());
    participantStatusEntity.setStudy(participantRegistrySiteEntity.getStudy());
    participantStatusEntity.setCreatedBy(participantRegistrySiteEntity.getCreatedBy());
    return participantStatusEntity;
  }
}
