/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.cloud.healthcare.fdamystudies.beans.AdminUserResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ManageUserAppBean;
import com.google.cloud.healthcare.fdamystudies.beans.ManageUsersResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SitesResponseBean;
import com.google.cloud.healthcare.fdamystudies.beans.StudiesResponseBean;
import com.google.cloud.healthcare.fdamystudies.beans.User;
import com.google.cloud.healthcare.fdamystudies.beans.UserAppPermissionRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserSitePermissionRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserStudyPermissionRequest;
import com.google.cloud.healthcare.fdamystudies.common.CommonConstants;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.config.AppPropertyConfig;
import com.google.cloud.healthcare.fdamystudies.mapper.UserMapper;
import com.google.cloud.healthcare.fdamystudies.model.AppEntity;
import com.google.cloud.healthcare.fdamystudies.model.AppPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.SitePermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.AppPermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.AppRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SitePermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyPermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyRepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;

@Service
public class ManageUserServiceImpl implements ManageUserService {

  private XLogger logger = XLoggerFactory.getXLogger(ManageUserServiceImpl.class.getName());

  @Autowired private UserRegAdminRepository userAdminRepository;

  @Autowired private AppRepository appRepository;

  @Autowired private StudyRepository studyRepository;

  @Autowired private SiteRepository siteRepository;

  @Autowired private AppPermissionRepository appPermissionRepository;

  @Autowired private StudyPermissionRepository studyPermissionRepository;

  @Autowired private SitePermissionRepository sitePermissionRepository;

  @Autowired private AppPropertyConfig appConfig;

  private boolean flagForViewApp;

  @Override
  @Transactional
  public AdminUserResponse createUser(UserRequest user) {
    logger.entry(String.format("createUser() with isSuperAdmin=%b", user.isSuperAdmin()));
    ErrorCode errorCode = validateUserRequest(user);
    if (errorCode != null) {
      logger.exit(String.format(CommonConstants.ERROR_CODE_LOG, errorCode));
      return new AdminUserResponse(errorCode);
    }

    AdminUserResponse userResponse =
        user.isSuperAdmin() ? saveSuperAdminDetails(user) : saveAdminDetails(user);

    logger.exit(String.format(CommonConstants.STATUS_LOG, userResponse.getHttpStatusCode()));
    return userResponse;
  }

  private ErrorCode validateUserRequest(UserRequest user) {
    logger.entry("validateUserRequest()");
    Optional<UserRegAdminEntity> optAdminDetails =
        userAdminRepository.findById(user.getSuperAdminUserId());
    if (!optAdminDetails.isPresent()) {
      return ErrorCode.USER_NOT_FOUND;
    }

    UserRegAdminEntity loggedInUserDetails = optAdminDetails.get();
    if (!loggedInUserDetails.isSuperAdmin()) {
      return ErrorCode.NOT_SUPER_ADMIN_ACCESS;
    }

    if (!user.isSuperAdmin()
        && (CollectionUtils.isEmpty(user.getApps()) || !hasAtleastOnePermission(user))) {
      return ErrorCode.PERMISSION_MISSING;
    }

    Optional<UserRegAdminEntity> optUsers = userAdminRepository.findByEmail(user.getEmail());
    logger.exit("Successfully validated user request");
    return optUsers.isPresent() ? ErrorCode.EMAIL_EXISTS : null;
  }

  private boolean hasAtleastOnePermission(UserRequest user) {
    logger.entry("hasAtleastOnePermission()");
    Predicate<UserAppPermissionRequest> appPredicate = app -> app.isSelected();
    Predicate<UserStudyPermissionRequest> studyPredicate = study -> study.isSelected();
    Predicate<UserSitePermissionRequest> sitePredicate = site -> site.isSelected();

    List<UserAppPermissionRequest> selectedApps =
        user.getApps().stream().filter(appPredicate).collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(selectedApps)) {
      return true;
    }

    for (UserAppPermissionRequest appPermission : user.getApps()) {
      List<UserStudyPermissionRequest> selectedStudies =
          CollectionUtils.emptyIfNull(appPermission.getStudies())
              .stream()
              .filter(studyPredicate)
              .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(selectedStudies)) {
        return true;
      }

      for (UserStudyPermissionRequest studyPermission : appPermission.getStudies()) {
        List<UserSitePermissionRequest> selectedSites =
            CollectionUtils.emptyIfNull(studyPermission.getSites())
                .stream()
                .filter(sitePredicate)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(selectedSites)) {
          return true;
        }
      }
    }

    logger.exit("No permissions found, return false");
    return false;
  }

  private AdminUserResponse saveAdminDetails(UserRequest user) {
    logger.entry("saveAdminDetails()");
    UserRegAdminEntity adminDetails =
        UserMapper.fromUserRequest(user, Long.valueOf(appConfig.getSecurityCodeExpireDate()));
    adminDetails = userAdminRepository.saveAndFlush(adminDetails);

    Map<Boolean, List<UserAppPermissionRequest>> groupBySelectedAppMap =
        user.getApps()
            .stream()
            .collect(Collectors.groupingBy(UserAppPermissionRequest::isSelected));

    // save permissions for selected apps
    for (UserAppPermissionRequest app :
        CollectionUtils.emptyIfNull(groupBySelectedAppMap.get(CommonConstants.SELECTED))) {
      saveAppStudySitePermissions(user, adminDetails, app);
    }

    // save permissions for unselected apps
    for (UserAppPermissionRequest app :
        CollectionUtils.emptyIfNull(groupBySelectedAppMap.get(CommonConstants.UNSELECTED))) {
      for (UserStudyPermissionRequest study : CollectionUtils.emptyIfNull(app.getStudies())) {
        if (study.isSelected()) {
          saveStudySitePermissions(user, adminDetails, study);
        } else if (CollectionUtils.isNotEmpty(study.getSites())) {
          saveSitePermissions(user, adminDetails, study);
        }
      }
    }
    logger.exit("Successfully saved admin details.");
    return new AdminUserResponse(MessageCode.ADD_NEW_USER_SUCCESS, adminDetails.getId());
  }

  private void saveAppStudySitePermissions(
      UserRequest user, UserRegAdminEntity adminDetails, UserAppPermissionRequest app) {
    logger.entry("saveAppStudySitePermissions()");
    Optional<AppEntity> optApp = appRepository.findById(app.getId());
    if (!optApp.isPresent()) {
      return;
    }

    AppEntity appDetails = optApp.get();
    AppPermissionEntity appPermission =
        UserMapper.newAppPermissionEntity(user, adminDetails, app, appDetails);
    appPermissionRepository.saveAndFlush(appPermission);

    List<StudyEntity> studies =
        (List<StudyEntity>) CollectionUtils.emptyIfNull(studyRepository.findByAppId(app.getId()));

    List<StudyPermissionEntity> studyPermissions =
        UserMapper.newStudyPermissionList(user, adminDetails, app, appDetails, studies);
    studyPermissionRepository.saveAll(studyPermissions);

    List<String> studyIds = studies.stream().map(StudyEntity::getId).collect(Collectors.toList());

    List<SiteEntity> sites =
        (List<SiteEntity>) CollectionUtils.emptyIfNull(siteRepository.findByStudyIds(studyIds));

    List<SitePermissionEntity> sitePermissions =
        UserMapper.newSitePermissionList(user, adminDetails, app, appDetails, sites);
    sitePermissionRepository.saveAll(sitePermissions);

    logger.exit("Successfully saved app study and site permissions.");
  }

  private void saveStudySitePermissions(
      UserRequest userId, UserRegAdminEntity superAdminDetails, UserStudyPermissionRequest study) {
    logger.entry("saveStudySitePermissions()");
    Optional<StudyEntity> optStudyInfo = studyRepository.findById(study.getStudyId());
    if (!optStudyInfo.isPresent()) {
      return;
    }
    List<SiteEntity> sites = siteRepository.findAll();
    StudyEntity studyDetails = optStudyInfo.get();
    StudyPermissionEntity studyPermission =
        UserMapper.newStudyPermissionEntity(userId, superAdminDetails, study, studyDetails);
    studyPermissionRepository.saveAndFlush(studyPermission);
    if (CollectionUtils.isNotEmpty(sites)) {
      for (SiteEntity site : sites) {
        if (site.getStudy().getId().equals(study.getStudyId())) {
          SitePermissionEntity sitePermission =
              UserMapper.newSitePermissionEntity(
                  userId, superAdminDetails, study, studyDetails, site);
          sitePermissionRepository.saveAndFlush(sitePermission);
        }
      }
    }
    logger.exit("Successfully saved study and site permissions.");
  }

  private void saveSitePermissions(
      UserRequest user, UserRegAdminEntity superAdminDetails, UserStudyPermissionRequest study) {
    for (UserSitePermissionRequest site : study.getSites()) {
      logger.entry("saveSitePermission()");
      if (site.isSelected()) {
        Optional<SiteEntity> optSite = siteRepository.findById(site.getSiteId());
        if (!optSite.isPresent()) {
          return;
        }
        SiteEntity siteDetails = optSite.get();
        SitePermissionEntity sitePermission =
            UserMapper.newSitePermissionEntity(user, site, superAdminDetails, siteDetails);
        sitePermissionRepository.saveAndFlush(sitePermission);
      }
    }
    logger.exit("Successfully saved site permissions.");
  }

  private AdminUserResponse saveSuperAdminDetails(UserRequest user) {
    logger.entry("saveSuperAdminDetails()");
    UserRegAdminEntity superAdminDetails =
        UserMapper.fromUserRequest(user, Long.valueOf(appConfig.getSecurityCodeExpireDate()));

    List<AppPermissionEntity> appPermissions =
        getAppPermissisonsForSuperAdmin(user, superAdminDetails);
    superAdminDetails.getAppPermissions().addAll(appPermissions);

    List<StudyPermissionEntity> studyPermissions =
        getStudyPermissisonsForSuperAdmin(user, superAdminDetails);
    superAdminDetails.getStudyPermissions().addAll(studyPermissions);

    List<SitePermissionEntity> sitePermissions =
        getSitePermissisonsForSuperAdmin(user, superAdminDetails);
    superAdminDetails.getSitePermissions().addAll(sitePermissions);

    userAdminRepository.saveAndFlush(superAdminDetails);

    logger.exit(String.format(CommonConstants.MESSAGE_CODE_LOG, MessageCode.ADD_NEW_USER_SUCCESS));
    return new AdminUserResponse(MessageCode.ADD_NEW_USER_SUCCESS, superAdminDetails.getId());
  }

  private List<SitePermissionEntity> getSitePermissisonsForSuperAdmin(
      UserRequest user, UserRegAdminEntity superAdminDetails) {
    logger.entry("getSitePermissisonsForSuperAdmin()");
    List<SiteEntity> sites =
        (List<SiteEntity>) CollectionUtils.emptyIfNull(siteRepository.findAll());
    List<SitePermissionEntity> sitePermissions = new ArrayList<>();
    for (SiteEntity siteInfo : sites) {
      SitePermissionEntity sitePermission =
          UserMapper.newSitePermissionEntity(user, null, superAdminDetails, siteInfo);
      sitePermissions.add(sitePermission);
    }

    logger.exit(String.format("total site permissions=%d", sitePermissions.size()));
    return sitePermissions;
  }

  private List<StudyPermissionEntity> getStudyPermissisonsForSuperAdmin(
      UserRequest user, UserRegAdminEntity superAdminDetails) {
    logger.entry("getStudyPermissisonsForSuperAdmin()");
    List<StudyEntity> studies =
        (List<StudyEntity>) CollectionUtils.emptyIfNull(studyRepository.findAll());
    List<StudyPermissionEntity> studyPermissions = new ArrayList<>();
    for (StudyEntity studyInfo : studies) {
      StudyPermissionEntity studyPermission =
          UserMapper.newStudyPermissionEntity(user, superAdminDetails, null, studyInfo);
      studyPermissions.add(studyPermission);
    }

    logger.exit(String.format("total study permissions=%d", studyPermissions.size()));
    return studyPermissions;
  }

  private List<AppPermissionEntity> getAppPermissisonsForSuperAdmin(
      UserRequest user, UserRegAdminEntity superAdminDetails) {
    logger.entry("getAppPermissisonsForSuperAdmin()");
    List<AppEntity> apps = (List<AppEntity>) CollectionUtils.emptyIfNull(appRepository.findAll());
    List<AppPermissionEntity> appPermissions = new ArrayList<>();
    for (AppEntity app : apps) {
      AppPermissionEntity appPermission =
          UserMapper.newAppPermissionEntity(user, superAdminDetails, app);
      appPermissions.add(appPermission);
    }

    logger.exit(String.format("total app permissions=%d", appPermissions.size()));
    return appPermissions;
  }

  @Override
  @Transactional
  public AdminUserResponse updateUser(UserRequest user, String superAdminUserId) {
    logger.entry(String.format("updateUser() with isSuperAdmin=%b", user.isSuperAdmin()));
    ErrorCode errorCode = validateUpdateUserRequest(user, superAdminUserId);
    if (errorCode != null) {
      logger.exit(String.format(CommonConstants.ERROR_CODE_LOG, errorCode));
      return new AdminUserResponse(errorCode);
    }

    AdminUserResponse userResponse =
        user.isSuperAdmin()
            ? updateSuperAdminDetails(user, superAdminUserId)
            : updateAdminDetails(user, superAdminUserId);

    logger.exit(String.format(CommonConstants.STATUS_LOG, userResponse.getHttpStatusCode()));
    return userResponse;
  }

  private ErrorCode validateUpdateUserRequest(UserRequest user, String superAdminUserId) {
    logger.entry("validateUpdateUserRequest()");
    Optional<UserRegAdminEntity> optAdminDetails = userAdminRepository.findById(superAdminUserId);
    if (!optAdminDetails.isPresent() || user.getUserId() == null) {
      return ErrorCode.USER_NOT_FOUND;
    }

    UserRegAdminEntity loggedInUserDetails = optAdminDetails.get();
    if (!loggedInUserDetails.isSuperAdmin()) {
      return ErrorCode.NOT_SUPER_ADMIN_ACCESS;
    }

    if (!user.isSuperAdmin() && !hasAtleastOnePermission(user)) {
      return ErrorCode.PERMISSION_MISSING;
    }
    logger.exit("Successfully validated user request");
    return null;
  }

  private AdminUserResponse updateSuperAdminDetails(UserRequest user, String superAdminUserId) {
    logger.entry("updateSuperAdminDetails()");
    Optional<UserRegAdminEntity> optAdminDetails = userAdminRepository.findById(user.getUserId());

    if (!optAdminDetails.isPresent()) {
      return new AdminUserResponse(ErrorCode.USER_NOT_FOUND);
    }

    UserRegAdminEntity adminDetails = optAdminDetails.get();
    adminDetails = UserMapper.fromUpdateUserRequest(user, adminDetails);

    deleteAllPermissions(user.getUserId());

    user.setSuperAdminUserId(superAdminUserId);

    List<AppPermissionEntity> appPermissions = getAppPermissisonsForSuperAdmin(user, adminDetails);
    adminDetails.getAppPermissions().addAll(appPermissions);

    List<StudyPermissionEntity> studyPermissions =
        getStudyPermissisonsForSuperAdmin(user, adminDetails);
    adminDetails.getStudyPermissions().addAll(studyPermissions);

    List<SitePermissionEntity> sitePermissions =
        getSitePermissisonsForSuperAdmin(user, adminDetails);
    adminDetails.getSitePermissions().addAll(sitePermissions);

    userAdminRepository.saveAndFlush(adminDetails);

    logger.exit(String.format(CommonConstants.MESSAGE_CODE_LOG, MessageCode.UPDATE_USER_SUCCESS));
    return new AdminUserResponse(MessageCode.UPDATE_USER_SUCCESS, adminDetails.getId());
  }

  private AdminUserResponse updateAdminDetails(UserRequest user, String superAdminUserId) {
    logger.entry("updateAdminDetails()");

    Optional<UserRegAdminEntity> optAdminDetails = userAdminRepository.findById(user.getUserId());

    if (!optAdminDetails.isPresent()) {
      return new AdminUserResponse(ErrorCode.USER_NOT_FOUND);
    }

    UserRegAdminEntity adminDetails = optAdminDetails.get();
    adminDetails = UserMapper.fromUpdateUserRequest(user, adminDetails);
    userAdminRepository.saveAndFlush(adminDetails);

    deleteAllPermissions(user.getUserId());

    user.setSuperAdminUserId(superAdminUserId);

    Map<Boolean, List<UserAppPermissionRequest>> groupBySelectedAppMap =
        user.getApps()
            .stream()
            .collect(Collectors.groupingBy(UserAppPermissionRequest::isSelected));

    // save permissions for selected apps
    for (UserAppPermissionRequest app :
        CollectionUtils.emptyIfNull(groupBySelectedAppMap.get(CommonConstants.SELECTED))) {
      saveAppStudySitePermissions(user, adminDetails, app);
    }

    // save permissions for unselected apps
    for (UserAppPermissionRequest app :
        CollectionUtils.emptyIfNull(groupBySelectedAppMap.get(CommonConstants.UNSELECTED))) {
      for (UserStudyPermissionRequest study : CollectionUtils.emptyIfNull(app.getStudies())) {
        if (study.isSelected()) {
          saveStudySitePermissions(user, adminDetails, study);
        } else if (CollectionUtils.isNotEmpty(study.getSites())) {
          saveSitePermissions(user, adminDetails, study);
        }
      }
    }
    logger.exit("Successfully updated admin details.");
    return new AdminUserResponse(MessageCode.UPDATE_USER_SUCCESS, adminDetails.getId());
  }

  private void deleteAllPermissions(String userId) {
    logger.entry("deleteAllPermissions()");
    sitePermissionRepository.deleteByAdminUserId(userId);
    studyPermissionRepository.deleteByAdminUserId(userId);
    appPermissionRepository.deleteByAdminUserId(userId);
    logger.exit("Successfully deleted all the assigned permissions.");
  }

  @Override
  public ManageUsersResponse getUsers(String userId, String adminId) {
    logger.entry("getUsers()");
    ErrorCode errorCode = validateGetUsersRequest(userId);
    if (errorCode != null) {
      logger.exit(String.format(CommonConstants.ERROR_CODE_LOG, errorCode));
      return new ManageUsersResponse(errorCode);
    }

    List<User> userList = new ArrayList<>();
    List<UserRegAdminEntity> adminList = getAdminRecords(adminId);
    if (CollectionUtils.isNotEmpty(adminList) && adminList.size() != 1) {
      adminList
          .stream()
          .map(admin -> userList.add(UserMapper.prepareUserInfo(admin)))
          .collect(Collectors.toList());
    } else {
      UserRegAdminEntity admin = adminList.get(0);
      User user = UserMapper.prepareUserInfo(admin);
      List<ManageUserAppBean> manageUsersAppList = new ArrayList<>();

      List<AppEntity> allExistingApps = appRepository.findAll();
      List<AppPermissionEntity> permittedApps =
          appPermissionRepository.findByAdminUserId(user.getId());

      for (AppEntity app : allExistingApps) {
        ManageUserAppBean manageApps = UserMapper.toManageUserAppBean(app);
        for (AppPermissionEntity appPermission : permittedApps) {
          if (app.getId().equals(appPermission.getAppInfo().getId())) {
            prepareAppPermission(admin, manageApps, appPermission);
          }
        }
        manageUsersAppList.add(manageApps);
      }

      for (ManageUserAppBean appResponse : CollectionUtils.emptyIfNull(manageUsersAppList)) {
        List<StudiesResponseBean> userStudies = new ArrayList<>();
        List<StudyEntity> studiesForApp = studyRepository.findByAppId(appResponse.getId());
        prepareStudyResponse(studiesForApp, userStudies, admin, appResponse.getId());
        appResponse.setStudies(userStudies);
        appResponse.setViewApp(flagForViewApp);

        prepareCountsForManageUserAppBean(appResponse);
        flagForViewApp = false;
      }

      user.setApps(manageUsersAppList);
      userList.add(user);
    }
    logger.exit(String.format(CommonConstants.STATUS_LOG, HttpStatus.OK.value()));
    return new ManageUsersResponse(MessageCode.MANAGE_USERS_SUCCESS, userList);
  }

  private void prepareCountsForManageUserAppBean(ManageUserAppBean appResponse) {
    logger.entry("prepareCountsForManageUserAppBean()");
    int selectedStudyCountPerApp = 0;
    int selectedSiteCountPerApp = 0;
    int totalSiteCountPerApp = 0;
    appResponse.setTotalStudiesCount(appResponse.getStudies().size());
    for (StudiesResponseBean study : CollectionUtils.emptyIfNull(appResponse.getStudies())) {
      if (study.isSelected()) {
        selectedStudyCountPerApp++;
      }

      prepareSiteCount(study);
      totalSiteCountPerApp += study.getTotalSitesCount();
      selectedSiteCountPerApp += study.getSelectedSitesCount();
    }

    appResponse.setSelectedStudiesCount(selectedStudyCountPerApp);
    appResponse.setTotalSitesCount(totalSiteCountPerApp);
    appResponse.setSelectedSitesCount(selectedSiteCountPerApp);
    logger.exit("Successfully prepared study and site counts");
  }

  private void prepareSiteCount(StudiesResponseBean study) {
    logger.entry("prepareStudyResponse()");
    int selectedSiteCountPerStudy = 0;
    study.setTotalSitesCount(study.getSites().size());
    for (SitesResponseBean site : CollectionUtils.emptyIfNull(study.getSites())) {
      if (site.isSelected()) {
        selectedSiteCountPerStudy++;
      }
    }

    study.setSelectedSitesCount(selectedSiteCountPerStudy);
    logger.exit("Successfully prepared site count");
  }

  private void prepareStudyResponse(
      List<StudyEntity> studiesForApp,
      List<StudiesResponseBean> userStudies,
      UserRegAdminEntity admin,
      String appId) {
    logger.entry("prepareStudyResponse()");
    for (StudyEntity existingStudy : CollectionUtils.emptyIfNull(studiesForApp)) {
      StudiesResponseBean studyResponse = UserMapper.toStudiesResponseBean(existingStudy);
      prepareStudyPermission(admin, appId, studyResponse);
      List<SitesResponseBean> userSites = new ArrayList<>();
      List<SiteEntity> sites = siteRepository.findByStudyId(studyResponse.getStudyId());
      for (SiteEntity site : CollectionUtils.emptyIfNull(sites)) {
        SitesResponseBean siteResponse = UserMapper.toSitesResponseBean(site);
        prepareSitePermission(site.getId(), admin, appId, siteResponse, studyResponse.getStudyId());
        userSites.add(siteResponse);
      }

      studyResponse.setSites(userSites);
      userStudies.add(studyResponse);
    }
    logger.exit("Successfully prepared study response");
  }

  private void prepareSitePermission(
      String siteId,
      UserRegAdminEntity admin,
      String appId,
      SitesResponseBean siteResponse,
      String studyId) {
    logger.entry("prepareSitePermission()");
    SitePermissionEntity sitePermission =
        sitePermissionRepository.findByAdminIdAndAppIdAndStudyIdAndSiteId(
            siteId, admin.getId(), appId, studyId);
    if (sitePermission == null) {
      return;
    }

    int permission = (admin.isSuperAdmin() || sitePermission.getCanEdit() == 1) ? 2 : 1;
    siteResponse.setPermission(permission);
    siteResponse.setSelected(true);
    siteResponse.setDisabled(false);
    flagForViewApp = true;
    logger.exit("Successfully prepared site permission");
  }

  private void prepareStudyPermission(
      UserRegAdminEntity admin, String appId, StudiesResponseBean studyResponse) {
    logger.entry("prepareStudyPermission()");
    StudyPermissionEntity studyPermission =
        studyPermissionRepository.findByAdminIdAndAppIdAndStudyId(
            admin.getId(), appId, studyResponse.getStudyId());
    if (studyPermission == null) {
      return;
    }

    int permission = (admin.isSuperAdmin() || studyPermission.getEdit() == 1) ? 2 : 1;
    studyResponse.setPermission(permission);
    studyResponse.setSelected(true);
    studyResponse.setDisabled(false);
    flagForViewApp = true;
    logger.exit("Successfully prepared study permission");
  }

  private void prepareAppPermission(
      UserRegAdminEntity admin, ManageUserAppBean manageApps, AppPermissionEntity appPermission) {
    logger.entry("prepareAppPermission()");
    int permission = (admin.isSuperAdmin() || appPermission.getEdit() == 1) ? 2 : 1;
    manageApps.setPermission(permission);
    manageApps.setSelected(true);
    manageApps.setDisabled(false);
    manageApps.setViewApp(true);
    logger.exit("Successfully prepared app permission");
  }

  private List<UserRegAdminEntity> getAdminRecords(String adminId) {
    logger.entry("getAdminRecords()");
    List<UserRegAdminEntity> userRegAdminList = new ArrayList<>();
    if (adminId != null) {
      Optional<UserRegAdminEntity> optAdminDetails = userAdminRepository.findById(adminId);
      if (optAdminDetails.isPresent()) {
        userRegAdminList.add(optAdminDetails.get());
      }

    } else {
      userRegAdminList = userAdminRepository.findAll();
    }

    logger.exit("Successfully fetched admin records");
    return userRegAdminList;
  }

  private ErrorCode validateGetUsersRequest(String loggedInAdminUserId) {
    logger.entry("validateUpdateUserRequest()");
    UserRegAdminEntity loggedInUserDetails;
    Optional<UserRegAdminEntity> optAdminDetails =
        userAdminRepository.findById(loggedInAdminUserId);
    if (!optAdminDetails.isPresent()) {
      return ErrorCode.USER_NOT_FOUND;
    }

    loggedInUserDetails = optAdminDetails.get();
    if (BooleanUtils.isFalse(loggedInUserDetails.isSuperAdmin())) {
      return ErrorCode.NOT_SUPER_ADMIN_ACCESS;
    }

    return null;
  }
}
