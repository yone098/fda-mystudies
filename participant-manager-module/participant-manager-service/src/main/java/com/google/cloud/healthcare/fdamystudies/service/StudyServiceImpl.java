/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package com.google.cloud.healthcare.fdamystudies.service;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.DISABLED_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.INVITED_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.NEW_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.OPEN_STUDY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetail;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryDetail;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryResponse;
import com.google.cloud.healthcare.fdamystudies.beans.StudyDetails;
import com.google.cloud.healthcare.fdamystudies.beans.StudyResponse;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.mapper.StudyMapper;
import com.google.cloud.healthcare.fdamystudies.model.AppEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.SitePermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.repository.AppRepository;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantRegistrySiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantStudyRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SitePermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyPermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyRepository;
import com.google.cloud.healthcare.fdamystudies.util.Constants;

@Service
public class StudyServiceImpl implements StudyService {
  private XLogger logger = XLoggerFactory.getXLogger(StudyServiceImpl.class.getName());

  @Autowired private StudyPermissionRepository studyPermissionRepository;

  @Autowired private ParticipantRegistrySiteRepository participantRegistrySiteRepository;

  @Autowired private ParticipantStudyRepository participantStudyRepository;

  @Autowired private SitePermissionRepository sitePermissionRepository;

  @Autowired private StudyRepository studyRepository;

  @Autowired private AppRepository appRepository;

  @Autowired private SiteRepository siteRepository;

  @Override
  @Transactional(readOnly = true)
  public StudyResponse getStudies(String userId) {
    logger.entry("getStudies(String userId)");

    List<SitePermissionEntity> sitePermissions =
        sitePermissionRepository.findSitePermissionByUserId(userId);

    if (CollectionUtils.isEmpty(sitePermissions)) {
      logger.exit(ErrorCode.STUDY_NOT_FOUND);
      return new StudyResponse(ErrorCode.STUDY_NOT_FOUND);
    }

    Map<StudyEntity, List<SitePermissionEntity>> studyPermissionMap =
        sitePermissions.stream().collect(Collectors.groupingBy(SitePermissionEntity::getStudy));

    List<String> usersStudyIds =
        sitePermissions
            .stream()
            .distinct()
            .map(studyEntity -> studyEntity.getStudy().getId())
            .collect(Collectors.toList());

    Map<String, StudyPermissionEntity> studyPermissionsByStudyInfoId =
        getStudyPermissionsByStudyInfoId(userId, usersStudyIds);

    List<String> usersSiteIds =
        sitePermissions
            .stream()
            .map(s -> s.getSite().getId())
            .distinct()
            .collect(Collectors.toList());

    Map<String, Long> siteWithInvitedParticipantCountMap =
        getSiteWithInvitedParticipantCountMap(usersSiteIds);

    Map<String, Long> siteWithEnrolledParticipantCountMap =
        getSiteWithEnrolledParticipantCountMap(usersSiteIds);

    return prepareStudyResponse(
        sitePermissions,
        studyPermissionsByStudyInfoId,
        studyPermissionMap,
        siteWithInvitedParticipantCountMap,
        siteWithEnrolledParticipantCountMap);
  }

  private Map<String, StudyPermissionEntity> getStudyPermissionsByStudyInfoId(
      String userId, List<String> usersStudyIds) {
    List<StudyPermissionEntity> studyPermissions =
        studyPermissionRepository.findStudyPermissionsOfUserByStudyIds(usersStudyIds, userId);

    Map<String, StudyPermissionEntity> studyPermissionsByStudyInfoId = new HashMap<>();
    if (CollectionUtils.isNotEmpty(studyPermissions)) {
      studyPermissionsByStudyInfoId =
          studyPermissions
              .stream()
              .collect(Collectors.toMap(e -> e.getStudy().getId(), Function.identity()));
    }
    return studyPermissionsByStudyInfoId;
  }

  private StudyResponse prepareStudyResponse(
      List<SitePermissionEntity> sitePermissions,
      Map<String, StudyPermissionEntity> studyPermissionsByStudyInfoId,
      Map<StudyEntity, List<SitePermissionEntity>> studyPermissionMap,
      Map<String, Long> siteWithInvitedParticipantCountMap,
      Map<String, Long> siteWithEnrolledParticipantCountMap) {
    List<StudyDetails> studies = new ArrayList<>();
    for (Map.Entry<StudyEntity, List<SitePermissionEntity>> entry : studyPermissionMap.entrySet()) {
      StudyDetails studyDetail = new StudyDetails();
      String studyId = entry.getKey().getId();
      studyDetail.setId(studyId);
      studyDetail.setCustomId(entry.getKey().getCustomId());
      studyDetail.setName(entry.getKey().getName());
      studyDetail.setType(entry.getKey().getType());
      studyDetail.setTotalSitesCount((long) entry.getValue().size());

      if (studyPermissionsByStudyInfoId.get(studyId) != null) {
        Integer studyEditPermission =
            studyPermissionsByStudyInfoId.get(entry.getKey().getId()).getEdit();
        studyDetail.setStudyPermission(
            studyEditPermission == Constants.VIEW_VALUE
                ? Constants.READ_PERMISSION
                : Constants.READ_AND_EDIT_PERMISSION);
        studyDetail.setStudyPermission(studyEditPermission);
      }

      calculateEnrollmentPercentage(
          siteWithInvitedParticipantCountMap,
          siteWithEnrolledParticipantCountMap,
          entry,
          studyDetail);
      studies.add(studyDetail);
    }

    StudyResponse studyResponse =
        new StudyResponse(MessageCode.GET_STUDIES_SUCCESS, studies, sitePermissions.size());
    logger.exit(String.format("total studies=%d", studyResponse.getStudies().size()));
    return studyResponse;
  }

  private void calculateEnrollmentPercentage(
      Map<String, Long> siteWithInvitedParticipantCountMap,
      Map<String, Long> siteWithEnrolledParticipantCountMap,
      Map.Entry<StudyEntity, List<SitePermissionEntity>> entry,
      StudyDetails studyDetail) {
    Long studyInvitedCount = 0L;
    Long studyEnrolledCount = 0L;
    for (SitePermissionEntity sitePermission : entry.getValue()) {
      studyInvitedCount =
          getStudyInvitedCount(
              siteWithInvitedParticipantCountMap, entry, studyInvitedCount, sitePermission);

      studyEnrolledCount =
          studyEnrolledCount
              + siteWithEnrolledParticipantCountMap.get(sitePermission.getSite().getId());
    }

    studyDetail.setEnrolled(studyEnrolledCount);
    studyDetail.setInvited(studyInvitedCount);
    if (studyDetail.getInvited() != 0 && studyDetail.getInvited() >= studyDetail.getEnrolled()) {
      Double percentage =
          (Double.valueOf(studyDetail.getEnrolled()) * 100)
              / Double.valueOf(studyDetail.getInvited());
      studyDetail.setEnrollmentPercentage(percentage);
    }
  }

  private Long getStudyInvitedCount(
      Map<String, Long> siteWithInvitedParticipantCountMap,
      Map.Entry<StudyEntity, List<SitePermissionEntity>> entry,
      Long studyInvitedCount,
      SitePermissionEntity sitePermission) {
    String siteId = sitePermission.getSite().getId();
    String studyType = entry.getKey().getType();
    if (siteWithInvitedParticipantCountMap.get(siteId) != null
        && studyType.equals(Constants.CLOSE_STUDY)) {
      studyInvitedCount = studyInvitedCount + siteWithInvitedParticipantCountMap.get(siteId);
    }

    if (studyType.equals(Constants.OPEN_STUDY)) {
      studyInvitedCount = studyInvitedCount + sitePermission.getSite().getTargetEnrollment();
    }
    return studyInvitedCount;
  }

  private Map<String, Long> getSiteWithEnrolledParticipantCountMap(List<String> usersSiteIds) {
    List<ParticipantStudyEntity> participantsEnrollments =
        participantStudyRepository.findParticipantsEnrollmentsOfSites(usersSiteIds);

    return participantsEnrollments
        .stream()
        .collect(Collectors.groupingBy(e -> e.getSite().getId(), Collectors.counting()));
  }

  private Map<String, Long> getSiteWithInvitedParticipantCountMap(List<String> usersSiteIds) {
    List<ParticipantRegistrySiteEntity> participantRegistry =
        participantRegistrySiteRepository.findParticipantRegistryOfSites(usersSiteIds);

    return participantRegistry
        .stream()
        .collect(
            Collectors.groupingBy(
                e -> e.getSite().getId(),
                Collectors.summingLong(ParticipantRegistrySiteEntity::getInvitationCount)));
  }

  @Override
  public ParticipantRegistryResponse getStudyParticipants(String userId, String studyId) {
    logger.entry("getStudyParticipants(String userId, String studyId)");

    /* if (StringUtils.isEmpty(studyId) || StringUtils.isEmpty(userId)) {
          logger.exit(ErrorCode.MISSING_REQUIRED_ARGUMENTS);
          return new ParticipantRegistryResponse(ErrorCode.MISSING_REQUIRED_ARGUMENTS);
        }
    */
    Optional<StudyPermissionEntity> optStudyPermission =
        studyPermissionRepository.findByStudyIdAndUserId(studyId, userId);

    if (!optStudyPermission.isPresent()) {
      logger.exit(ErrorCode.STUDY_NOT_FOUND);
      return new ParticipantRegistryResponse(ErrorCode.STUDY_NOT_FOUND);
    }

    Optional<StudyEntity> optStudy = studyRepository.findById(studyId);
    Optional<AppEntity> optApps =
        appRepository.findById(optStudyPermission.get().getAppInfo().getId());

    // TODO(Navya) if condition check with old code
    if (!optStudy.isPresent() || !optApps.isPresent()) {
      logger.exit(ErrorCode.STUDY_NOT_FOUND);
      return new ParticipantRegistryResponse(ErrorCode.STUDY_NOT_FOUND);
    }

    ParticipantRegistryDetail participantRegistryDetail =
        setValuesForParticipantDetail(studyId, optStudy, optApps);

    List<ParticipantStudyEntity> participantStudiesList =
        participantStudyRepository.findParticipantsByStudies(studyId);

    return preapreRegistryPartcipantResponse(participantStudiesList, participantRegistryDetail);
  }

  private ParticipantRegistryDetail setValuesForParticipantDetail(
      String studyId, Optional<StudyEntity> study, Optional<AppEntity> app) {
    ParticipantRegistryDetail participantRegistryDetail =
        StudyMapper.fromStudyAndApp(study.get(), app.get());

    List<SiteEntity> sites = siteRepository.findByStudyId(studyId);

    if (!sites.isEmpty() && OPEN_STUDY.equalsIgnoreCase(study.get().getType())) {
      for (SiteEntity site : sites) {
        participantRegistryDetail.setTargetEnrollment(site.getTargetEnrollment());
      }
    }
    return participantRegistryDetail;
  }

  private ParticipantRegistryResponse preapreRegistryPartcipantResponse(
      List<ParticipantStudyEntity> participantStudiesList,
      ParticipantRegistryDetail participantRegistryDetail) {
    List<ParticipantDetail> registryParticipants = new ArrayList<>();
    for (ParticipantStudyEntity participantStudy : participantStudiesList) {

      ParticipantDetail participantDetail = StudyMapper.fromParticipantStudy(participantStudy);

      String status = participantStudy.getParticipantRegistrySite().getOnboardingStatus();

      if ("I".equalsIgnoreCase(status) || "E".equalsIgnoreCase(status)) {
        participantDetail.setOnboardingStatus(INVITED_STATUS);
      } else if ("N".equalsIgnoreCase(status)) {
        participantDetail.setOnboardingStatus(NEW_STATUS);
      } else {
        participantDetail.setOnboardingStatus(DISABLED_STATUS);
      }

      registryParticipants.add(participantDetail);
    }
    participantRegistryDetail.setRegistryParticipants(registryParticipants);

    ParticipantRegistryResponse participantRegistryResponse =
        new ParticipantRegistryResponse(
            MessageCode.GET_PARTICIPANT_REGISTRY_SUCCESS, participantRegistryDetail);
    // TODO (Navya)setting only message
    logger.exit(String.format("message=%s", participantRegistryResponse.getMessage()));
    return participantRegistryResponse;
  }
}
