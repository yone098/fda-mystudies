/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.mapper;

import com.google.cloud.healthcare.fdamystudies.beans.User;
import com.google.cloud.healthcare.fdamystudies.beans.UserAppDetails;
import com.google.cloud.healthcare.fdamystudies.beans.UserAppPermissionRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserSiteDetails;
import com.google.cloud.healthcare.fdamystudies.beans.UserSitePermissionRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserStudyDetails;
import com.google.cloud.healthcare.fdamystudies.beans.UserStudyPermissionRequest;
import com.google.cloud.healthcare.fdamystudies.common.IdGenerator;
import com.google.cloud.healthcare.fdamystudies.common.Permission;
import com.google.cloud.healthcare.fdamystudies.common.UserStatus;
import com.google.cloud.healthcare.fdamystudies.model.AppEntity;
import com.google.cloud.healthcare.fdamystudies.model.AppPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.SitePermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;

public final class UserMapper {

  private UserMapper() {}

  public static UserRegAdminEntity fromUserRequest(
      UserRequest userRequest, long securityCodeExpireTime) {
    UserRegAdminEntity admin = new UserRegAdminEntity();
    admin.setEmail(userRequest.getEmail());
    admin.setFirstName(userRequest.getFirstName());
    admin.setLastName(userRequest.getLastName());
    admin.setCreatedBy(userRequest.getSuperAdminUserId());
    admin.setEmailChanged(false);
    admin.setStatus(UserStatus.INVITED.getValue());
    admin.setSuperAdmin(userRequest.isSuperAdmin());
    admin.setSecurityCode(IdGenerator.id());
    admin.setSecurityCodeExpireDate(
        new Timestamp(
            Instant.now().plus(securityCodeExpireTime, ChronoUnit.MINUTES).toEpochMilli()));
    Integer manageLocation =
        userRequest.isSuperAdmin()
            ? Permission.READ_EDIT.value()
            : userRequest.getManageLocations();
    admin.setLocationPermission(manageLocation);
    return admin;
  }

  public static UserRegAdminEntity fromUpdateUserRequest(
      UserRequest userRequest, UserRegAdminEntity adminDetails) {
    adminDetails.setFirstName(userRequest.getFirstName());
    adminDetails.setLastName(userRequest.getLastName());
    adminDetails.setSuperAdmin(userRequest.isSuperAdmin());
    Integer manageLocation =
        userRequest.isSuperAdmin()
            ? Permission.READ_EDIT.value()
            : userRequest.getManageLocations();
    adminDetails.setLocationPermission(manageLocation);
    return adminDetails;
  }

  public static SitePermissionEntity newSitePermissionEntity(
      UserRequest user,
      UserSitePermissionRequest site,
      UserRegAdminEntity superAdminDetails,
      SiteEntity siteDetails) {
    SitePermissionEntity sitePermission = new SitePermissionEntity();
    sitePermission.setAppInfo(siteDetails.getStudy().getAppInfo());
    sitePermission.setStudy(siteDetails.getStudy());
    sitePermission.setSite(siteDetails);
    sitePermission.setCreatedBy(user.getSuperAdminUserId());
    Integer edit =
        site != null && site.getPermission() == Permission.READ_VIEW.value()
            ? Permission.READ_VIEW.value()
            : Permission.READ_EDIT.value();
    sitePermission.setCanEdit(edit);
    sitePermission.setUrAdminUser(superAdminDetails);
    return sitePermission;
  }

  public static SitePermissionEntity newSitePermissionEntity(
      UserRequest user,
      UserRegAdminEntity superAdminDetails,
      UserStudyPermissionRequest study,
      StudyEntity studyDetails,
      SiteEntity site) {
    SitePermissionEntity sitePermission = new SitePermissionEntity();
    sitePermission.setAppInfo(studyDetails.getAppInfo());
    sitePermission.setCreatedBy(user.getSuperAdminUserId());
    Integer edit =
        study.getPermission() == Permission.READ_VIEW.value()
            ? Permission.READ_VIEW.value()
            : Permission.READ_EDIT.value();
    sitePermission.setCanEdit(edit);
    sitePermission.setStudy(studyDetails);
    sitePermission.setSite(site);
    sitePermission.setUrAdminUser(superAdminDetails);
    return sitePermission;
  }

  public static List<SitePermissionEntity> newSitePermissionList(
      UserRequest user,
      UserRegAdminEntity superAdminDetails,
      UserAppPermissionRequest app,
      AppEntity appDetails,
      List<SiteEntity> sites) {
    List<SitePermissionEntity> sitePermissions = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(sites)) {
      for (SiteEntity siteEntity : sites) {
        SitePermissionEntity sitePermission = new SitePermissionEntity();
        sitePermission.setAppInfo(appDetails);
        sitePermission.setCreatedBy(user.getSuperAdminUserId());
        Integer edit =
            app != null && app.getPermission() == Permission.READ_VIEW.value()
                ? Permission.READ_VIEW.value()
                : Permission.READ_EDIT.value();
        sitePermission.setCanEdit(edit);
        sitePermission.setStudy(siteEntity.getStudy());
        sitePermission.setSite(siteEntity);
        sitePermission.setUrAdminUser(superAdminDetails);
        sitePermissions.add(sitePermission);
      }
    }

    return sitePermissions;
  }

  public static StudyPermissionEntity newStudyPermissionEntity(
      UserRequest user,
      UserRegAdminEntity superAdminDetails,
      UserStudyPermissionRequest study,
      StudyEntity studyDetails) {
    StudyPermissionEntity studyPermission = new StudyPermissionEntity();
    studyPermission.setAppInfo(studyDetails.getAppInfo());
    studyPermission.setStudy(studyDetails);
    studyPermission.setCreatedBy(user.getSuperAdminUserId());
    Integer edit =
        study != null && study.getPermission() == Permission.READ_VIEW.value()
            ? Permission.READ_VIEW.value()
            : Permission.READ_EDIT.value();
    studyPermission.setEdit(edit);
    studyPermission.setUrAdminUser(superAdminDetails);
    return studyPermission;
  }

  public static List<StudyPermissionEntity> newStudyPermissionList(
      UserRequest userRequest,
      UserRegAdminEntity superAdminDetails,
      UserAppPermissionRequest appRequest,
      AppEntity appDetails,
      List<StudyEntity> studies) {
    List<StudyPermissionEntity> studyPermissions = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(studies)) {
      for (StudyEntity studyEntity : studies) {
        StudyPermissionEntity studyPermission = new StudyPermissionEntity();
        studyPermission.setAppInfo(appDetails);
        studyPermission.setCreatedBy(userRequest.getSuperAdminUserId());
        Integer edit =
            appRequest != null && appRequest.getPermission() == Permission.READ_VIEW.value()
                ? Permission.READ_VIEW.value()
                : Permission.READ_EDIT.value();
        studyPermission.setEdit(edit);
        studyPermission.setStudy(studyEntity);
        studyPermission.setUrAdminUser(superAdminDetails);
        studyPermissions.add(studyPermission);
      }
    }
    return studyPermissions;
  }

  public static AppPermissionEntity newAppPermissionEntity(
      UserRequest user, UserRegAdminEntity superAdminDetails, AppEntity app) {
    AppPermissionEntity appPermission = new AppPermissionEntity();
    appPermission.setAppInfo(app);
    appPermission.setCreatedBy(user.getSuperAdminUserId());
    appPermission.setEdit(Permission.READ_EDIT.value());
    appPermission.setUrAdminUser(superAdminDetails);
    return appPermission;
  }

  public static AppPermissionEntity newAppPermissionEntity(
      UserRequest userRequest,
      UserRegAdminEntity superAdminDetails,
      UserAppPermissionRequest app,
      AppEntity appDetails) {
    AppPermissionEntity appPermission = new AppPermissionEntity();
    appPermission.setAppInfo(appDetails);
    appPermission.setCreatedBy(userRequest.getSuperAdminUserId());
    Integer edit =
        app != null && app.getPermission() == Permission.READ_VIEW.value()
            ? Permission.READ_VIEW.value()
            : Permission.READ_EDIT.value();
    appPermission.setEdit(edit);
    appPermission.setUrAdminUser(superAdminDetails);
    return appPermission;
  }

  public static UserAppDetails toUserAppDetails(AppEntity app) {
    UserAppDetails userApp = new UserAppDetails();
    userApp.setId(app.getId());
    userApp.setCustomId(app.getAppId());
    userApp.setName(app.getAppName());
    return userApp;
  }

  public static UserStudyDetails toStudiesResponseBean(StudyEntity existingStudy) {
    UserStudyDetails studyResponse = new UserStudyDetails();
    studyResponse.setStudyId(existingStudy.getId());
    studyResponse.setCustomStudyId(existingStudy.getCustomId());
    studyResponse.setStudyName(existingStudy.getName());
    return studyResponse;
  }

  public static UserSiteDetails toSitesResponseBean(SiteEntity site) {
    UserSiteDetails siteResponse = new UserSiteDetails();
    siteResponse.setSiteId(site.getId());
    siteResponse.setLocationId(site.getLocation().getId());
    siteResponse.setCustomLocationId(site.getLocation().getCustomId());
    siteResponse.setLocationName(site.getLocation().getName());
    siteResponse.setLocationDescription(site.getLocation().getDescription());
    return siteResponse;
  }

  public static User prepareUserInfo(UserRegAdminEntity admin) {
    User user = new User();
    user.setId(admin.getId());
    user.setEmail(admin.getEmail());
    user.setFirstName(admin.getFirstName());
    user.setLastName(admin.getLastName());
    user.setSuperAdmin(admin.isSuperAdmin());
    user.setManageLocations(admin.getLocationPermission());
    UserStatus userStatus = UserStatus.fromValue(admin.getStatus());
    user.setStatus(userStatus.getDescription());
    return user;
  }

  public static UserStudyDetails toAppStudyResponse(StudyEntity study, List<SiteEntity> sites) {
    UserStudyDetails userStudiesResponse = new UserStudyDetails();
    userStudiesResponse.setStudyId(study.getId());
    userStudiesResponse.setCustomStudyId(study.getCustomId());
    userStudiesResponse.setStudyName(study.getName());
    List<UserSiteDetails> appSiteResponsesList =
        CollectionUtils.emptyIfNull(sites)
            .stream()
            .map(UserMapper::toUserSitesResponse)
            .collect(Collectors.toList());
    userStudiesResponse.getSites().addAll(appSiteResponsesList);
    return null;
  }

  public static UserSiteDetails toUserSitesResponse(SiteEntity site) {
    UserSiteDetails userSitesResponse = new UserSiteDetails();
    userSitesResponse.setSiteId(site.getId());
    userSitesResponse.setCustomLocationId(site.getLocation().getCustomId());
    userSitesResponse.setLocationDescription(site.getLocation().getDescription());
    userSitesResponse.setLocationId(site.getLocation().getId());
    userSitesResponse.setLocationName(site.getLocation().getName());
    return userSitesResponse;
  }
}
