/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.repository;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.google.cloud.healthcare.fdamystudies.model.AppPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyConsentBO;

@Repository
@ConditionalOnProperty(
    value = "participant.manager.repository.enabled",
    havingValue = "true",
    matchIfMissing = false)
public interface StudyConsentRepository extends JpaRepository<StudyConsentBO, String> {

  @Query("SELECT sc FROM StudyConsentBO sc WHERE sc.participantStudy.id = ")
  public List<AppPermissionEntity> findAppPermissionsOfUserByAppIds(
      @Param("appIds") List<String> usersAppsIds, String userId);
}
