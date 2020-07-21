/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.ACTIVE_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.D;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.ENROLLED_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.OPEN;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.OPEN_STUDY;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.STATUS_ACTIVE;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.STATUS_ENROLLED;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.YET_TO_JOIN;
import static com.google.cloud.healthcare.fdamystudies.util.Constants.ACTIVE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.cloud.healthcare.fdamystudies.beans.DecomissionSiteRequest;
import com.google.cloud.healthcare.fdamystudies.beans.DecomissionSiteResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SiteResponse;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.common.Permission;
import com.google.cloud.healthcare.fdamystudies.common.SiteStatus;
import com.google.cloud.healthcare.fdamystudies.mapper.ParticipantMapper;
import com.google.cloud.healthcare.fdamystudies.mapper.SiteMapper;
import com.google.cloud.healthcare.fdamystudies.model.AppPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.LocationEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.SitePermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.repository.AppPermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.LocationRepository;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantRegistrySiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantStudyRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SitePermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyPermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyRepository;

@Service
public class SiteServiceImpl implements SiteService {

  private XLogger logger = XLoggerFactory.getXLogger(SiteServiceImpl.class.getName());

  @Autowired private SiteRepository siteRepository;

  @Autowired private LocationRepository locationRepository;

  @Autowired private StudyRepository studyRepository;

  @Autowired private StudyPermissionRepository studyPermissionRepository;

  @Autowired private AppPermissionRepository appPermissionRepository;

  @Autowired private ParticipantRegistrySiteRepository participantRegistrySiteRepository;

  @Autowired private SitePermissionRepository sitePermissionRepository;

  @Autowired private ParticipantStudyRepository participantStudyRepository;

  @Override
  @Transactional
  public SiteResponse addSite(SiteRequest siteRequest) {
    logger.entry("begin addSite()");
    boolean allowed = isEditPermissionAllowed(siteRequest);

    if (!allowed) {
      logger.exit(
          String.format(
              "Add site for locationId=%s and studyId=%s failed with error code=%s",
              siteRequest.getLocationId(),
              siteRequest.getStudyId(),
              ErrorCode.SITE_PERMISSION_ACEESS_DENIED));
      return new SiteResponse(ErrorCode.SITE_PERMISSION_ACEESS_DENIED);
    }

    Optional<SiteEntity> optSiteEntity =
        siteRepository.findByLocationIdAndStudyId(
            siteRequest.getLocationId(), siteRequest.getStudyId());

    if (optSiteEntity.isPresent()) {
      logger.warn(
          String.format(
              "Add site for locationId=%s and studyId=%s failed with error code=%s",
              siteRequest.getLocationId(), siteRequest.getStudyId(), ErrorCode.SITE_EXISTS));
      return new SiteResponse(ErrorCode.SITE_EXISTS);
    }

    SiteResponse siteResponse =
        saveSiteWithSitePermissions(
            siteRequest.getStudyId(), siteRequest.getLocationId(), siteRequest.getUserId());
    logger.exit(
        String.format(
            "Site %s added to locationId=%s and studyId=%s",
            siteResponse.getSiteId(), siteRequest.getLocationId(), siteRequest.getStudyId()));
    return new SiteResponse(siteResponse.getSiteId(), MessageCode.ADD_SITE_SUCCESS);
  }

  private boolean isEditPermissionAllowed(SiteRequest siteRequest) {
    logger.entry("isEditPermissionAllowed(siteRequest)");
    Optional<StudyPermissionEntity> optStudyPermissionEntity =
        studyPermissionRepository.findByStudyIdAndUserId(
            siteRequest.getStudyId(), siteRequest.getUserId());

    if (optStudyPermissionEntity.isPresent()) {
      StudyPermissionEntity studyPermission = optStudyPermissionEntity.get();
      String appInfoId = studyPermission.getAppInfo().getId();
      Optional<AppPermissionEntity> optAppPermissionEntity =
          appPermissionRepository.findByUserIdAndAppId(siteRequest.getUserId(), appInfoId);
      if (optAppPermissionEntity.isPresent()) {
        AppPermissionEntity appPermission = optAppPermissionEntity.get();
        logger.exit(String.format("editValue=%d", Permission.READ_EDIT.value()));
        return studyPermission.getEdit() == Permission.READ_EDIT.value()
            || appPermission.getEdit() == Permission.READ_EDIT.value();
      }
    }
    logger.exit("default permission is edit, return true");
    return true;
  }

  private SiteResponse saveSiteWithSitePermissions(
      String studyId, String locationId, String userId) {
    logger.entry("saveSiteWithStudyPermission()");

    List<StudyPermissionEntity> userStudypermissionList =
        studyPermissionRepository.findByStudyId(studyId);

    SiteEntity site = new SiteEntity();
    Optional<StudyEntity> studyInfo = studyRepository.findById(studyId);
    if (studyInfo.isPresent()) {
      site.setStudy(studyInfo.get());
    }
    Optional<LocationEntity> location = locationRepository.findById(locationId);
    if (location.isPresent()) {
      site.setLocation(location.get());
    }
    site.setCreatedBy(userId);
    site.setStatus(ACTIVE);
    addSitePermissions(userId, userStudypermissionList, site);
    site = siteRepository.save(site);

    logger.exit(
        String.format(
            "saved siteId=%s with %d site permissions",
            site.getId(), site.getSitePermissions().size()));
    return SiteMapper.toSiteResponse(site);
  }

  private void addSitePermissions(
      String userId, List<StudyPermissionEntity> userStudypermissionList, SiteEntity site) {
    for (StudyPermissionEntity studyPermission : userStudypermissionList) {
      Integer editPermission =
          studyPermission.getUrAdminUser().getId().equals(userId)
              ? Permission.READ_EDIT.value()
              : studyPermission.getEdit();
      SitePermissionEntity sitePermission = new SitePermissionEntity();
      sitePermission.setUrAdminUser(studyPermission.getUrAdminUser());
      sitePermission.setStudy(studyPermission.getStudy());
      sitePermission.setAppInfo(studyPermission.getAppInfo());
      sitePermission.setCanEdit(editPermission);
      sitePermission.setCreatedBy(userId);
      site.addSitePermissionEntity(sitePermission);
    }
  }

  public boolean isEditPermissionAllowed(String studyId, String userId) {
    logger.entry("isEditPermissionAllowed(siteRequest)");
    Optional<StudyPermissionEntity> optStudyPermissionEntity =
        studyPermissionRepository.findByStudyIdAndUserId(studyId, userId);

    if (optStudyPermissionEntity.isPresent()) {
      StudyPermissionEntity studyPermission = optStudyPermissionEntity.get();
      String appInfoId = studyPermission.getAppInfo().getId();
      Optional<AppPermissionEntity> optAppPermissionEntity =
          appPermissionRepository.findByUserIdAndAppId(userId, appInfoId);
      if (optAppPermissionEntity.isPresent()) {
        AppPermissionEntity appPermission = optAppPermissionEntity.get();
        logger.exit(String.format("editValue=%d", Permission.READ_EDIT.value()));
        return studyPermission.getEdit() == Permission.READ_EDIT.value()
            || appPermission.getEdit() == Permission.READ_EDIT.value();
      }
    }
    logger.exit("default permission is edit, return true");
    return true;
  }

  @Override
  @Transactional
  public DecomissionSiteResponse decomissionSite(DecomissionSiteRequest decomissionSiteRequest) {
    logger.entry("decomissionSite()");

    ErrorCode errorCode = validateDecommissionSiteRequest(decomissionSiteRequest);
    if (errorCode != null) {
      logger.exit(errorCode);
      return new DecomissionSiteResponse(errorCode);
    }

    Optional<SiteEntity> optSiteEntity =
        siteRepository.findById(decomissionSiteRequest.getSiteId());
    if (optSiteEntity.isPresent()) {
      SiteEntity site = optSiteEntity.get();

      if (site.getStatus().equals(SiteStatus.DEACTIVE.value())) {
        site.setStatus(SiteStatus.ACTIVE.value());
        site = siteRepository.saveAndFlush(site);
        logger.exit(
            String.format(
                "Site Recommissioned successfully status=%d,  message code=%s",
                site.getStatus(), MessageCode.RECOMMISSION_SITE_SUCCESS));
        return new DecomissionSiteResponse(site.getStatus(), MessageCode.RECOMMISSION_SITE_SUCCESS);
      }
      site.setStatus(SiteStatus.DEACTIVE.value());
      siteRepository.saveAndFlush(site);
      setPermissions(decomissionSiteRequest.getSiteId());
      logger.exit(
          String.format(
              "Site Decommissioned successfully status=%d,  message code=%s",
              site.getStatus(), MessageCode.DECOMMISSION_SITE_SUCCESS));
      return new DecomissionSiteResponse(site.getStatus(), MessageCode.DECOMMISSION_SITE_SUCCESS);
    }

    return null;
  }

  private ErrorCode validateDecommissionSiteRequest(DecomissionSiteRequest decomissionSiteRequest) {
    List<SitePermissionEntity> sitePermissions =
        sitePermissionRepository.findByUserIdAndSiteId(
            decomissionSiteRequest.getUserId(), decomissionSiteRequest.getSiteId());

    if (CollectionUtils.isEmpty(sitePermissions)) {
      return ErrorCode.SITE_NOT_FOUND;
    }

    Iterator<SitePermissionEntity> iterator = sitePermissions.iterator();
    SitePermissionEntity sitePermission = new SitePermissionEntity();
    while (iterator.hasNext()) {
      sitePermission = iterator.next();
      if (OPEN.equalsIgnoreCase(sitePermission.getStudy().getType())) {
        return ErrorCode.OPEN_STUDY_FOR_DECOMMISSION_SITE;
      }
    }
    String studyId = sitePermission.getStudy().getId();
    List<String> status = Arrays.asList(STATUS_ENROLLED, STATUS_ACTIVE);
    Long participantStudiesCount =
        participantStudyRepository.findByStudyIdAndStatus(status, studyId);

    boolean canEdit = isEditPermissionAllowed(studyId, decomissionSiteRequest.getUserId());
    if (!canEdit || participantStudiesCount > 0) {
      return ErrorCode.SITE_PERMISSION_ACEESS_DENIED;
    }

    return null;
  }

  private void setPermissions(String siteId) {

    List<SitePermissionEntity> sitePermissions = sitePermissionRepository.findBySiteId(siteId);
    List<String> siteAdminIdList = new ArrayList<>();
    List<String> studyIdList = new ArrayList<>();
    List<String> studyAdminIdList = new ArrayList<>();

    for (SitePermissionEntity sitePermission : sitePermissions) {
      studyIdList.add(sitePermission.getStudy().getId());
      siteAdminIdList.add(sitePermission.getUrAdminUser().getId());
    }

    List<StudyPermissionEntity> studyPermissions =
        studyPermissionRepository.findByByUserIdsAndStudyIds(siteAdminIdList, studyIdList);

    for (StudyPermissionEntity studyPermission : studyPermissions) {
      studyAdminIdList.add(studyPermission.getUrAdminUser().getId());
    }

    for (SitePermissionEntity sitePermission : sitePermissions) {
      if (!studyAdminIdList.contains(sitePermission.getUrAdminUser().getId())) {
        sitePermissionRepository.delete(sitePermission);
      } else {
        sitePermission.setCanEdit(Permission.READ_VIEW.value());
        sitePermissionRepository.saveAndFlush(sitePermission);
      }
    }
    String status = YET_TO_JOIN;
    List<ParticipantStudyEntity> participantStudies =
        participantStudyRepository.findBySiteIdAndStatus(siteId, status);

    if (CollectionUtils.isNotEmpty(participantStudies)) {
      for (ParticipantStudyEntity participantStudy : participantStudies) {
        String participantRegistrySiteId = participantStudy.getParticipantRegistrySite().getId();
        Optional<ParticipantRegistrySiteEntity> optParticipantRegistrySite =
            participantRegistrySiteRepository.findById(participantRegistrySiteId);
        if (optParticipantRegistrySite.isPresent()) {
          ParticipantRegistrySiteEntity participantRegistrySiteEntity =
              optParticipantRegistrySite.get();
          participantRegistrySiteEntity.setOnboardingStatus(D);
          participantRegistrySiteRepository.saveAndFlush(participantRegistrySiteEntity);
        }
      }
    }
  }

  @Override
  @Transactional
  public ParticipantResponse addNewParticipant(ParticipantRequest participant, String userId) {
    logger.entry("begin addNewParticipant()");

    Optional<SiteEntity> optSite = siteRepository.findById(participant.getSiteId());

    ErrorCode errorCode = validate(participant, userId, optSite);
    if (errorCode != null) {
      logger.exit(errorCode.getDescription());
      return new ParticipantResponse(errorCode);
    }
    StudyEntity study = optSite.get().getStudy();
    List<ParticipantRegistrySiteEntity> registry =
        participantRegistrySiteRepository.findParticipantRegistrySitesByStudyIdAndEmail(
            study.getId(), participant.getEmail());

    if (CollectionUtils.isNotEmpty(registry)) {
      return extracted(registry);
    }

    ParticipantRegistrySiteEntity participantRegistrySite =
        ParticipantMapper.fromParticipantRequest(participant, optSite.get());
    participantRegistrySite.setCreatedBy(userId);
    participantRegistrySiteRepository.save(participantRegistrySite);
    logger.exit(String.format("participantRegistrySiteId=%s", participantRegistrySite.getId()));
    return ParticipantMapper.toParticipantResponse(participantRegistrySite);
  }

  private ParticipantResponse extracted(List<ParticipantRegistrySiteEntity> registry) {
    ParticipantRegistrySiteEntity participantRegistrySite = registry.get(0);
    Optional<ParticipantStudyEntity> participantStudy =
        participantStudyRepository.findParticipantsEnrollmentsByParticipantRegistrySite(
            participantRegistrySite.getId());
    // TODO chk with old code
    if (participantStudy.isPresent()
        && ENROLLED_STATUS.equals(participantStudy.get().getStatus())) {
      logger.exit(ErrorCode.ENROLLED_PARTICIPANT.getDescription());
      return new ParticipantResponse(ErrorCode.ENROLLED_PARTICIPANT);
    } else {
      logger.exit(ErrorCode.EMAIL_EXISTS.getDescription());
      return new ParticipantResponse(ErrorCode.EMAIL_EXISTS);
    }
  }

  private ErrorCode validate(
      ParticipantRequest participant, String userId, Optional<SiteEntity> optSite) {

    if (!optSite.isPresent() || optSite.get().getStatus() != ACTIVE_STATUS) {
      return ErrorCode.SITE_NOT_EXIST_OR_INACTIVE;
    }

    Optional<SitePermissionEntity> optSitePermission =
        sitePermissionRepository.findSitePermissionByUserIdAnsSiteId(
            userId, participant.getSiteId());

    if (!optSitePermission.isPresent()
        || optSitePermission.get().getCanEdit() != Permission.READ_EDIT.value()) {
      return ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED;
    }

    if (optSite.get().getStudy() != null && OPEN_STUDY.equals(optSite.get().getStudy().getType())) {
      return ErrorCode.OPEN_STUDY;
    }
    return null;
  }
}
