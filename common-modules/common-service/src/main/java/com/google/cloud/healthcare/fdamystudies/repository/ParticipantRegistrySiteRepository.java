/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package com.google.cloud.healthcare.fdamystudies.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteCount;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;

@ConditionalOnProperty(
    value = "participant.manager.repository.enabled",
    havingValue = "true",
    matchIfMissing = false)
@Repository
public interface ParticipantRegistrySiteRepository
    extends JpaRepository<ParticipantRegistrySiteEntity, String> {

  @Query("SELECT pr FROM ParticipantRegistrySiteEntity pr WHERE pr.site.id in (:siteIds)")
  public List<ParticipantRegistrySiteEntity> findBySiteIds(@Param("siteIds") List<String> siteIds);

  @Query("SELECT pr FROM ParticipantRegistrySiteEntity pr WHERE pr.id in (:ids)")
  public List<ParticipantRegistrySiteEntity> findByIds(@Param("ids") List<String> ids);

  @Query(
      "SELECT pr FROM ParticipantRegistrySiteEntity pr "
          + "where pr.study.id = :studyId and pr.email = :email")
  public Optional<ParticipantRegistrySiteEntity> findByStudyIdAndEmail(
      String studyId, String email);

  @Query(
      "SELECT pr FROM ParticipantRegistrySiteEntity pr "
          + "where pr.site.id = :siteId and pr.onboardingStatus = :onboardingStatus order by created desc")
  public List<ParticipantRegistrySiteEntity> findBySiteIdAndStatus(
      String siteId, String onboardingStatus);

  @Query(
      "SELECT pr.onboardingStatus AS onboardingStatus, count(pr.email) AS count FROM ParticipantRegistrySiteEntity pr WHERE pr.site.id= :siteId group by pr.onboardingStatus")
  public List<ParticipantRegistrySiteCount> findParticipantRegistrySitesCountBySiteId(
      String siteId);

  @Query("SELECT pr FROM ParticipantRegistrySiteEntity pr WHERE pr.site.id =:siteId")
  public List<ParticipantRegistrySiteEntity> findBySiteId(String siteId);
  // TODO(N) optional already exist
  @Query(
      "SELECT pr FROM ParticipantRegistrySiteEntity pr where pr.study.id = :studyId and pr.email = :email")
  public List<ParticipantRegistrySiteEntity> findByStudyIdAndEmail1(String studyId, String email);

  // TODO(N) query
  @Query("UPDATE ParticipantRegistrySiteEntity set onboardingStatus = :status Where id in (:ids)")
  public List<ParticipantRegistrySiteEntity> updateStatus(String status, List<String> ids);
}
