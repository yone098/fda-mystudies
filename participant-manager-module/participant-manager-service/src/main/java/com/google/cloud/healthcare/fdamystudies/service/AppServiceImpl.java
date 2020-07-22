/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.CLOSE_STUDY;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.OPEN_STUDY;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.READ_AND_EDIT_PERMISSION;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.READ_PERMISSION;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.VIEW_VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.cloud.healthcare.fdamystudies.beans.AppDetails;
import com.google.cloud.healthcare.fdamystudies.beans.AppParticipantRegistryResponse;
import com.google.cloud.healthcare.fdamystudies.beans.AppResponse;
import com.google.cloud.healthcare.fdamystudies.beans.AppStudyResponse;
import com.google.cloud.healthcare.fdamystudies.beans.EnrolledStudies;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.Participants;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.mapper.AppMapper;
import com.google.cloud.healthcare.fdamystudies.mapper.ParticipantMapper;
import com.google.cloud.healthcare.fdamystudies.mapper.StudyMapper;
import com.google.cloud.healthcare.fdamystudies.model.AppEntity;
import com.google.cloud.healthcare.fdamystudies.model.AppPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.SitePermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserDetailsEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.AppPermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.AppRepository;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantRegistrySiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantStudyRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SitePermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyRepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserDetailsRepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;

@Service
public class AppServiceImpl implements AppService {
  private XLogger logger = XLoggerFactory.getXLogger(AppServiceImpl.class.getName());

  @Autowired private AppPermissionRepository appPermissionRepository;

  @Autowired private UserDetailsRepository userDetailsRepository;

  @Autowired private ParticipantRegistrySiteRepository participantRegistrySiteRepository;

  @Autowired private ParticipantStudyRepository participantStudiesRepository;

  @Autowired private SitePermissionRepository sitePermissionRepository;

  @Autowired private UserRegAdminRepository userRegAdminRepository;

  @Autowired private AppRepository appRepository;

  @Autowired private StudyRepository studyRepository;

  @Autowired private SiteRepository siteRepository;

  @Override
  @Transactional(readOnly = true)
  public AppResponse getApps(String userId) {
    logger.entry("getApps(userId)");

    List<SitePermissionEntity> sitePermissions =
        sitePermissionRepository.findSitePermissionByUserId(userId);
    if (CollectionUtils.isEmpty(sitePermissions)) {
      logger.exit(ErrorCode.APP_NOT_FOUND);
      return new AppResponse(ErrorCode.APP_NOT_FOUND);
    }

    List<String> appIds = getAppIds(sitePermissions);

    Map<String, AppPermissionEntity> appPermissionsByAppInfoId =
        getAppPermissionsMap(userId, appIds);

    Map<String, Long> appIdbyUsersCount = userDetailsRepository.findAppUsersCount(appIds);

    Map<AppEntity, Map<StudyEntity, List<SitePermissionEntity>>>
        sitePermissionByAppInfoAndStudyInfo = getPermissionByAppInfoAndStudyInfo(sitePermissions);

    List<String> usersSiteIds = getUserSiteIds(sitePermissions);

    List<ParticipantRegistrySiteEntity> participantRegistry =
        participantRegistrySiteRepository.findParticipantRegistryOfSites(usersSiteIds);

    Map<String, Long> siteWithInvitedParticipantCountMap =
        getSiteWithInvitedParticipantCountMap(participantRegistry);

    List<ParticipantStudyEntity> participantsEnrollments =
        participantStudiesRepository.findParticipantsEnrollmentsOfSites(usersSiteIds);

    Map<String, Long> siteWithEnrolledParticipantCountMap =
        participantsEnrollments
            .stream()
            .collect(Collectors.groupingBy(e -> e.getSite().getId(), Collectors.counting()));

    return prepareAppResponse(
        sitePermissions,
        appPermissionsByAppInfoId,
        appIdbyUsersCount,
        sitePermissionByAppInfoAndStudyInfo,
        siteWithInvitedParticipantCountMap,
        siteWithEnrolledParticipantCountMap);
  }

  private AppResponse prepareAppResponse(
      List<SitePermissionEntity> sitePermissions,
      Map<String, AppPermissionEntity> appPermissionsByAppInfoId,
      Map<String, Long> appIdbyUsersCount,
      Map<AppEntity, Map<StudyEntity, List<SitePermissionEntity>>>
          sitePermissionByAppInfoAndStudyInfo,
      Map<String, Long> siteWithInvitedParticipantCountMap,
      Map<String, Long> siteWithEnrolledParticipantCountMap) {
    List<AppDetails> apps = new ArrayList<>();
    for (Map.Entry<AppEntity, Map<StudyEntity, List<SitePermissionEntity>>> entry :
        sitePermissionByAppInfoAndStudyInfo.entrySet()) {
      AppDetails appDetails = new AppDetails();
      appDetails.setId(entry.getKey().getId());
      appDetails.setCustomId(entry.getKey().getAppId());
      appDetails.setTotalStudiesCount((long) entry.getValue().size());
      appDetails.setName(entry.getKey().getAppName());
      appDetails.setAppUsersCount(appIdbyUsersCount.get(entry.getKey().getId()));
      appDetails.setAppUsersCount(0L);

      if (appPermissionsByAppInfoId.get(entry.getKey().getId()) != null) {
        Integer appEditPermission = appPermissionsByAppInfoId.get(entry.getKey().getId()).getEdit();
        appDetails.setAppPermission(
            appEditPermission == VIEW_VALUE ? READ_PERMISSION : READ_AND_EDIT_PERMISSION);
      }

      calculateEnrollmentPercentage(
          siteWithInvitedParticipantCountMap,
          siteWithEnrolledParticipantCountMap,
          entry,
          appDetails);
      apps.add(appDetails);
    }

    Map<StudyEntity, List<SitePermissionEntity>> studyPermissionMap =
        sitePermissions.stream().collect(Collectors.groupingBy(SitePermissionEntity::getStudy));

    AppResponse appResponse =
        new AppResponse(MessageCode.GET_APPS_SUCCESS, apps, studyPermissionMap.size());
    logger.exit(String.format("total apps=%d", appResponse.getApps().size()));
    return appResponse;
  }

  private void calculateEnrollmentPercentage(
      Map<String, Long> siteWithInvitedParticipantCountMap,
      Map<String, Long> siteWithEnrolledParticipantCountMap,
      Map.Entry<AppEntity, Map<StudyEntity, List<SitePermissionEntity>>> entry,
      AppDetails appDetails) {
    long appInvitedCount = 0L;
    long appEnrolledCount = 0L;
    for (Map.Entry<StudyEntity, List<SitePermissionEntity>> studyEntry :
        entry.getValue().entrySet()) {
      String studyType = studyEntry.getKey().getType();
      for (SitePermissionEntity sitePermission : studyEntry.getValue()) {
        String siteId = sitePermission.getSite().getId();
        if (siteWithInvitedParticipantCountMap.get(siteId) != null
            && CLOSE_STUDY.equals(studyType)) {
          appInvitedCount = appInvitedCount + siteWithInvitedParticipantCountMap.get(siteId);
        }

        if (OPEN_STUDY.equals(studyType)) {
          appInvitedCount = appInvitedCount + sitePermission.getSite().getTargetEnrollment();
        }

        if (siteWithEnrolledParticipantCountMap.get(siteId) != null) {
          appEnrolledCount = appEnrolledCount + siteWithEnrolledParticipantCountMap.get(siteId);
        }
      }
    }
    appDetails.setEnrolledCount(appEnrolledCount);
    appDetails.setInvitedCount(appInvitedCount);
    double percentage = 0;
    if (appDetails.getInvitedCount() != 0
        && appDetails.getInvitedCount() >= appDetails.getEnrolledCount()) {
      percentage =
          (Double.valueOf(appDetails.getEnrolledCount()) * 100)
              / Double.valueOf(appDetails.getInvitedCount());
      appDetails.setEnrollmentPercentage(percentage);
    }
  }

  private Map<String, Long> getSiteWithInvitedParticipantCountMap(
      List<ParticipantRegistrySiteEntity> participantRegistry) {
    return participantRegistry
        .stream()
        .collect(
            Collectors.groupingBy(
                e -> e.getSite().getId(),
                Collectors.summingLong(ParticipantRegistrySiteEntity::getInvitationCount)));
  }

  private List<String> getUserSiteIds(List<SitePermissionEntity> sitePermissions) {
    return sitePermissions
        .stream()
        .map(s -> s.getSite().getId())
        .distinct()
        .collect(Collectors.toList());
  }

  private Map<AppEntity, Map<StudyEntity, List<SitePermissionEntity>>>
      getPermissionByAppInfoAndStudyInfo(List<SitePermissionEntity> sitePermissions) {
    return sitePermissions
        .stream()
        .collect(
            Collectors.groupingBy(
                SitePermissionEntity::getAppInfo,
                Collectors.groupingBy(SitePermissionEntity::getStudy)));
  }

  private Map<String, AppPermissionEntity> getAppPermissionsMap(
      String userId, List<String> appIds) {
    Map<String, AppPermissionEntity> appPermissionsByAppInfoId = null;

    List<AppPermissionEntity> appPermissions =
        appPermissionRepository.findAppPermissionsOfUserByAppIds(appIds, userId);

    if (CollectionUtils.isNotEmpty(appPermissions)) {
      appPermissionsByAppInfoId =
          appPermissions
              .stream()
              .collect(Collectors.toMap(e -> e.getAppInfo().getId(), Function.identity()));
    }
    return appPermissionsByAppInfoId;
  }

  private List<String> getAppIds(List<SitePermissionEntity> sitePermissions) {
    return sitePermissions
        .stream()
        .map(appInfoDetailsbo -> appInfoDetailsbo.getAppInfo().getId())
        .distinct()
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public AppResponse getAppsWithOptionalFields(String userId, String[] fields) {
    logger.entry("getAppsWithOptionalFields(userId,fields)");

    Optional<UserRegAdminEntity> optUserRegAdminEntity =
        userRegAdminRepository.findByUserRegAdminId(userId);

    if (!(optUserRegAdminEntity.isPresent() && optUserRegAdminEntity.get().isSuperAdmin())) {
      logger.exit(ErrorCode.USER_ADMIN_ACCESS_DENIED);
      return new AppResponse(ErrorCode.USER_ADMIN_ACCESS_DENIED);
    }
    List<AppEntity> apps = appRepository.findAllApps();
    List<String> appIds = getAppInfo(apps);

    List<StudyEntity> studies = studyRepository.findByAppIds(appIds);
    List<String> studyIds = getStudyIds(studies);

    List<SiteEntity> sites = siteRepository.findBySites(studyIds);

    AppResponse appResponse = prepareAppResponse(apps, studies, sites, fields);

    logger.exit(String.format("total apps=%d", appResponse.getApps().size()));
    return appResponse;
  }

  private AppResponse prepareAppResponse(
      List<AppEntity> apps, List<StudyEntity> studies, List<SiteEntity> sites, String[] fields) {
    Map<String, List<StudyEntity>> groupByAppIdStudyMap =
        studies.stream().collect(Collectors.groupingBy(StudyEntity::getAppId));

    Map<String, List<SiteEntity>> groupByStudyIdSiteMap =
        sites.stream().collect(Collectors.groupingBy(SiteEntity::getStudyId));

    List<AppDetails> appsList = new ArrayList<>();
    for (AppEntity app : apps) {
      AppDetails appDetails = AppMapper.toAppDetails(app);
      if (ArrayUtils.contains(fields, "studies")) {
        List<AppStudyResponse> appStudyResponses =
            StudyMapper.toAppDetailsResponseList(
                groupByAppIdStudyMap.get(app.getId()), groupByStudyIdSiteMap, fields);

        appDetails.getStudies().addAll(appStudyResponses);
      }
      appsList.add(appDetails);
    }

    return new AppResponse(MessageCode.GET_APPS_DETAILS_SUCCESS, appsList);
  }

  private List<String> getStudyIds(List<StudyEntity> studyEntity) {
    List<String> studyIdList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(studyEntity)) {
      studyEntity
          .stream()
          .map(study -> studyIdList.add(study.getId()))
          .collect(Collectors.toList());
    }
    return studyIdList;
  }

  private List<String> getAppInfo(List<AppEntity> appEntity) {
    List<String> appInfoIdList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(appEntity)) {
      appEntity.stream().map(app -> appInfoIdList.add(app.getId())).collect(Collectors.toList());
    }
    return appInfoIdList;
  }

  @Override
  @Transactional(readOnly = true)
  public ParticipantResponse getAppParticipantRegistry(String appId, String adminId) {
    logger.entry("getAppParticipantRegistry(appId,adminId)");
    Optional<AppPermissionEntity> optAppPermissionEntity =
        appPermissionRepository.findByUserIdAndAppId(adminId, appId);
    if (!optAppPermissionEntity.isPresent()) {
      logger.exit(ErrorCode.APP_NOT_FOUND);
      return new ParticipantResponse(ErrorCode.APP_NOT_FOUND);
    }

    AppPermissionEntity appPermission = optAppPermissionEntity.get();
    AppParticipantRegistryResponse appDetails = new AppParticipantRegistryResponse();
    appDetails.setId(appPermission.getAppInfo().getId());
    appDetails.setCustomId(appPermission.getAppInfo().getAppId());
    appDetails.setName(appPermission.getAppInfo().getAppName());

    List<UserDetailsEntity> userDetails =
        userDetailsRepository.findByAppId(appPermission.getAppInfo().getId());
    List<StudyEntity> studyEntity = studyRepository.findByAppId(appPermission.getAppInfo().getId());

    Map<String, Map<StudyEntity, List<ParticipantStudyEntity>>>
        participantEnrollmentsByUserDetailsAndStudy =
            getEnrolledParticipants(userDetails, studyEntity);

    List<AppParticipantRegistryResponse> appsList = new ArrayList<>();

    List<Participants> participants =
        prepareParticpantDetails(userDetails, participantEnrollmentsByUserDetailsAndStudy);
    appDetails.setParticipants(participants);

    appsList.add(appDetails);

    ParticipantResponse apps =
        new ParticipantResponse(MessageCode.GET_APP_PARTICIPANT_REGISTRY_SUCCESS, appsList);
    logger.exit(String.format("total apps=%d", apps.getAppParticipantRegistryResponse().size()));
    return apps;
  }

  private Map<String, Map<StudyEntity, List<ParticipantStudyEntity>>> getEnrolledParticipants(
      List<UserDetailsEntity> userDetails, List<StudyEntity> studyEntity) {
    List<String> appsStudyIds =
        studyEntity.stream().distinct().map(s -> s.getId()).collect(Collectors.toList());

    List<String> userDetailsIds =
        userDetails.stream().distinct().map(a -> a.getId()).collect(Collectors.toList());

    List<ParticipantStudyEntity> participantEnrollments =
        participantStudiesRepository.findByAppIdAndUserId(appsStudyIds, userDetailsIds);

    return participantEnrollments
        .stream()
        .collect(
            Collectors.groupingBy(
                s -> s.getUserDetails().getId(), Collectors.groupingBy(s -> s.getStudy())));
  }

  private List<Participants> prepareParticpantDetails(
      List<UserDetailsEntity> userDetails,
      Map<String, Map<StudyEntity, List<ParticipantStudyEntity>>>
          participantEnrollmentsByUserDetailsAndStudy) {
    List<Participants> participantList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(userDetails)) {
      for (UserDetailsEntity userDetailsEntity : userDetails) {
        List<EnrolledStudies> enrolledStudies = new ArrayList<>();
        Participants participant =
            ParticipantMapper.toParticipantDetails(
                userDetailsEntity, participantEnrollmentsByUserDetailsAndStudy, enrolledStudies);
        participantList.add(participant);
      }
    }
    return participantList;
  }
}
