/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.mapper;

import com.google.cloud.healthcare.fdamystudies.common.EnrollmentStatus;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantEnrollmentHistoryEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;

public class ParticipantStatusHistoryMapper {

  public static ParticipantEnrollmentHistoryEntity toParticipantStatusHistoryEntity(
      ParticipantRegistrySiteEntity participantRegistrySiteEntity, EnrollmentStatus enrollment) {
    ParticipantEnrollmentHistoryEntity participantStatusEntity =
        new ParticipantEnrollmentHistoryEntity();
    participantStatusEntity.setStatus(enrollment.getStatus());
    participantStatusEntity.setSite(participantRegistrySiteEntity.getSite());
    participantStatusEntity.setStudy(participantRegistrySiteEntity.getStudy());
    return participantStatusEntity;
  }
}
