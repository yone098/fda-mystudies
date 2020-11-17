/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.repository;

import com.google.cloud.healthcare.fdamystudies.model.AddNewAdminEmailServiceEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyIdAndParticipantRegistryId;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(
    value = "participant.manager.repository.enabled",
    havingValue = "true",
    matchIfMissing = false)
public interface AddNewAdminEmailServiceRepository
    extends JpaRepository<AddNewAdminEmailServiceEntity, String> {

  @Query(
      value = "SELECT DISTINCT email FROM new_admin_email_service WHERE status = 0",
      nativeQuery = true)
  public List<StudyIdAndParticipantRegistryId> findAllWithStatusZero();
}
