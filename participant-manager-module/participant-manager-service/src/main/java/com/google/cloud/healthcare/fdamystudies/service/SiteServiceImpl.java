/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.ACTIVE_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.CLOSE_STUDY;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.D;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.ENROLLED_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.OPEN;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.OPEN_STUDY;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.STATUS_ACTIVE;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.YET_TO_JOIN;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.cloud.healthcare.fdamystudies.beans.ConsentHistory;
import com.google.cloud.healthcare.fdamystudies.beans.DecomissionSiteRequest;
import com.google.cloud.healthcare.fdamystudies.beans.DecomissionSiteResponse;
import com.google.cloud.healthcare.fdamystudies.beans.EmailRequest;
import com.google.cloud.healthcare.fdamystudies.beans.EmailResponse;
import com.google.cloud.healthcare.fdamystudies.beans.Enrollments;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetailResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetails;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryDetail;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.Site;
import com.google.cloud.healthcare.fdamystudies.beans.SiteDetails;
import com.google.cloud.healthcare.fdamystudies.beans.SiteRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SiteResponse;
import com.google.cloud.healthcare.fdamystudies.beans.StudyDetails;
import com.google.cloud.healthcare.fdamystudies.common.CommonConstants;
import com.google.cloud.healthcare.fdamystudies.common.DateTimeUtils;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.common.OnboardingStatus;
import com.google.cloud.healthcare.fdamystudies.common.Permission;
import com.google.cloud.healthcare.fdamystudies.common.SiteStatus;
import com.google.cloud.healthcare.fdamystudies.config.AppPropertyConfig;
import com.google.cloud.healthcare.fdamystudies.mapper.ConsentMapper;
import com.google.cloud.healthcare.fdamystudies.mapper.ParticipantMapper;
import com.google.cloud.healthcare.fdamystudies.mapper.SiteMapper;
import com.google.cloud.healthcare.fdamystudies.mapper.StudyMapper;
import com.google.cloud.healthcare.fdamystudies.model.AppPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.LocationEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.SitePermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyConsentEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.repository.AppPermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.LocationRepository;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantRegistrySiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantStudyRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SitePermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyConsentRepository;
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

  @Autowired private AppPropertyConfig appPropertyConfig;

  @Autowired private EmailService emailService;

  @Autowired private StudyServiceImpl studyServiceImpl;

  @Autowired private StudyConsentRepository studyConsentRepository;

  @Override
  @Transactional
  public SiteResponse addSite(SiteRequest siteRequest) {
    logger.entry("begin addSite()");
    boolean allowed = isEditPermissionAllowed(siteRequest.getStudyId(), siteRequest.getUserId());

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
    site.setStatus(SiteStatus.ACTIVE.value());
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
                "Site Recommissioned successfully siteId=%s, status=%d,  message code=%s",
                site.getId(), site.getStatus(), MessageCode.RECOMMISSION_SITE_SUCCESS));
        return new DecomissionSiteResponse(
            site.getId(), site.getStatus(), MessageCode.RECOMMISSION_SITE_SUCCESS);
      }

      site.setStatus(SiteStatus.DEACTIVE.value());
      siteRepository.saveAndFlush(site);

      setPermissions(decomissionSiteRequest.getSiteId());

      logger.exit(
          String.format(
              "Site Decommissioned successfully siteId=%s, status=%d,  message code=%s",
              site.getId(), site.getStatus(), MessageCode.DECOMMISSION_SITE_SUCCESS));
      return new DecomissionSiteResponse(
          site.getId(), site.getStatus(), MessageCode.DECOMMISSION_SITE_SUCCESS);
    }

    return null;
  }

  private ErrorCode validateDecommissionSiteRequest(DecomissionSiteRequest decomissionSiteRequest) {
    List<SitePermissionEntity> sitePermissions =
        sitePermissionRepository.findByUserIdAndSiteId(
            decomissionSiteRequest.getUserId(), decomissionSiteRequest.getSiteId());

    if (CollectionUtils.isEmpty(sitePermissions)) {
      logger.exit(String.format("Site not found  error_code=%s", ErrorCode.SITE_NOT_FOUND));
      return ErrorCode.SITE_NOT_FOUND;
    }

    Iterator<SitePermissionEntity> iterator = sitePermissions.iterator();
    SitePermissionEntity sitePermission = new SitePermissionEntity();
    while (iterator.hasNext()) {
      sitePermission = iterator.next();
      if (OPEN.equalsIgnoreCase(sitePermission.getStudy().getType())) {
        logger.exit(
            String.format(
                "Cannot decomission site as studyType is open error_code=%s",
                ErrorCode.OPEN_STUDY_FOR_DECOMMISSION_SITE));
        return ErrorCode.OPEN_STUDY_FOR_DECOMMISSION_SITE;
      }
    }
    String studyId = sitePermission.getStudy().getId();
    List<String> status = Arrays.asList(ENROLLED_STATUS, STATUS_ACTIVE);
    Long participantStudiesCount =
        participantStudyRepository.findByStudyIdAndStatus(status, studyId);

    boolean canEdit = isEditPermissionAllowed(studyId, decomissionSiteRequest.getUserId());
    if (!canEdit || participantStudiesCount > 0) {
      logger.exit(
          String.format(
              "Does not have permission to maintain site, error_code=%s",
              ErrorCode.SITE_PERMISSION_ACEESS_DENIED));
      return ErrorCode.SITE_PERMISSION_ACEESS_DENIED;
    }

    return null;
  }

  private void setPermissions(String siteId) {

    List<SitePermissionEntity> sitePermissions = sitePermissionRepository.findBySiteId(siteId);
    List<String> siteAdminIdList = new ArrayList<>();
    List<String> studyIdList = new ArrayList<>();

    for (SitePermissionEntity sitePermission : sitePermissions) {
      studyIdList.add(sitePermission.getStudy().getId());
      siteAdminIdList.add(sitePermission.getUrAdminUser().getId());
    }

    List<StudyPermissionEntity> studyPermissions =
        studyPermissionRepository.findByByUserIdsAndStudyIds(siteAdminIdList, studyIdList);

    List<String> studyAdminIdList = new ArrayList<>();
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
    deactivateYetToEnrollParticipants(siteId);
  }

  private void deactivateYetToEnrollParticipants(String siteId) {

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

    if (!optSite.isPresent() || !optSite.get().getStatus().equals(ACTIVE_STATUS)) {
      logger.exit(ErrorCode.SITE_NOT_EXIST_OR_INACTIVE);
      return new ParticipantResponse(ErrorCode.SITE_NOT_EXIST_OR_INACTIVE);
    }

    SiteEntity site = optSite.get();
    ErrorCode errorCode = validationForAddNewParticipant(participant, userId, site);
    if (errorCode != null) {
      logger.exit(errorCode);
      return new ParticipantResponse(errorCode);
    }

    ParticipantRegistrySiteEntity participantRegistrySite =
        ParticipantMapper.fromParticipantRequest(participant, site);
    participantRegistrySite.setCreatedBy(userId);
    participantRegistrySite =
        participantRegistrySiteRepository.saveAndFlush(participantRegistrySite);
    ParticipantResponse response =
        new ParticipantResponse(
            MessageCode.ADD_PARTICIPANT_SUCCESS, participantRegistrySite.getId());

    logger.exit(String.format("participantRegistrySiteId=%s", participantRegistrySite.getId()));
    return response;
  }

  private ErrorCode validationForAddNewParticipant(
      ParticipantRequest participant, String userId, SiteEntity site) {
    Optional<SitePermissionEntity> optSitePermission =
        sitePermissionRepository.findSitePermissionByUserIdAndSiteId(
            userId, participant.getSiteId());

    if (!optSitePermission.isPresent()
        || !optSitePermission.get().getCanEdit().equals(Permission.READ_EDIT.value())) {
      return ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED;
    }

    if (site.getStudy() != null && OPEN_STUDY.equals(site.getStudy().getType())) {
      return ErrorCode.OPEN_STUDY;
    }

    Optional<ParticipantRegistrySiteEntity> registry =
        participantRegistrySiteRepository.findParticipantRegistrySitesByStudyIdAndEmail(
            site.getStudy().getId(), participant.getEmail());

    if (registry.isPresent()) {
      ParticipantRegistrySiteEntity participantRegistrySite = registry.get();
      Optional<ParticipantStudyEntity> participantStudy =
          participantStudyRepository.findParticipantsEnrollmentsByParticipantRegistrySite(
              participantRegistrySite.getId());

      if (participantStudy.isPresent()
          && ENROLLED_STATUS.equals(participantStudy.get().getStatus())) {
        return ErrorCode.ENROLLED_PARTICIPANT;
      } else {
        return ErrorCode.EMAIL_EXISTS;
      }
    }
    return null;
  }

  @Override
  public InviteParticipantResponse inviteParticipants(
      InviteParticipantRequest inviteParticipantRequest) {
    Optional<SiteEntity> optSiteEntity =
        siteRepository.findById(inviteParticipantRequest.getSiteId());

    if (!optSiteEntity.isPresent()
        || !optSiteEntity.get().getStatus().equals(CommonConstants.ACTIVE_STATUS)) {
      return new InviteParticipantResponse(ErrorCode.SITE_NOT_EXIST);
    }

    Optional<SitePermissionEntity> optSitePermissionEntity =
        sitePermissionRepository.findSitePermissionByUserIdAndSiteId(
            inviteParticipantRequest.getUserId(), inviteParticipantRequest.getSiteId());

    if (!optSitePermissionEntity.isPresent()
        || Permission.READ_EDIT
            != Permission.fromValue(optSitePermissionEntity.get().getCanEdit())) {
      return new InviteParticipantResponse(ErrorCode.NO_PERMISSION_TO_MANAGE_SITE);
    }

    List<ParticipantRegistrySiteEntity> listOfparticipants =
        participantRegistrySiteRepository.findParticipantRegistryById(
            inviteParticipantRequest.getIds());

    SiteEntity siteEntity = optSiteEntity.get();
    List<ParticipantRegistrySiteEntity> succeededEmailParticipants =
        sendEmailForListOfParticipants(listOfparticipants, siteEntity);
    participantRegistrySiteRepository.saveAll(succeededEmailParticipants);

    InviteParticipantResponse inviteParticipantResponse =
        succeededEmailParticipants.isEmpty()
            ? new InviteParticipantResponse(ErrorCode.EMAIL_FAILED_TO_IMPORT)
            : new InviteParticipantResponse(MessageCode.PARTICIPANTS_INVITED_SUCCESS);

    inviteParticipantResponse.setIds(inviteParticipantRequest.getIds());
    listOfparticipants.removeAll(succeededEmailParticipants);
    List<String> failedInvitations =
        listOfparticipants.stream().map(email -> email.getEmail()).collect(Collectors.toList());
    inviteParticipantResponse.setFailedInvitations(failedInvitations);
    List<String> successIds =
        succeededEmailParticipants.stream().map(ids -> ids.getId()).collect(Collectors.toList());
    inviteParticipantResponse.setSuccessIds(successIds);

    return inviteParticipantResponse;
  }

  public List<ParticipantRegistrySiteEntity> sendEmailForListOfParticipants(
      List<ParticipantRegistrySiteEntity> listOfparticipants, SiteEntity siteEntity) {
    List<ParticipantRegistrySiteEntity> succeededEmail = new ArrayList<>();
    for (ParticipantRegistrySiteEntity participantRegistrySiteEntity : listOfparticipants) {
      if (participantRegistrySiteEntity != null
          && (OnboardingStatus.INVITED
                  == OnboardingStatus.fromCode(participantRegistrySiteEntity.getOnboardingStatus())
              || OnboardingStatus.NEW
                  == OnboardingStatus.fromCode(
                      participantRegistrySiteEntity.getOnboardingStatus()))) {

        String token = RandomStringUtils.randomAlphanumeric(8);
        participantRegistrySiteEntity.setEnrollmentToken(token);
        participantRegistrySiteEntity.setInvitationDate(
            new Timestamp(Instant.now().toEpochMilli()));

        if (OnboardingStatus.NEW
            == OnboardingStatus.fromCode(participantRegistrySiteEntity.getOnboardingStatus())) {
          participantRegistrySiteEntity.setInvitationCount(
              participantRegistrySiteEntity.getInvitationCount() + 1);
          participantRegistrySiteEntity.setOnboardingStatus(OnboardingStatus.INVITED.getCode());
        }

        participantRegistrySiteEntity.setEnrollmentTokenExpiry(
            new Timestamp(
                Instant.now()
                    .plus(appPropertyConfig.getEnrollmentTokenExpiryinHours(), ChronoUnit.HOURS)
                    .toEpochMilli()));

        sendEmailToInviteParticipant(participantRegistrySiteEntity, siteEntity);
        succeededEmail.add(participantRegistrySiteEntity);
      }
    }
    return succeededEmail;
  }

  private EmailResponse sendEmailToInviteParticipant(
      ParticipantRegistrySiteEntity participantRegistrySiteEntity, SiteEntity siteEntity) {
    Map<String, String> templateArgs = new HashMap<>();
    templateArgs.put("study name", siteEntity.getStudy().getName());
    templateArgs.put("org name", siteEntity.getStudy().getAppInfo().getOrgInfo().getName());
    templateArgs.put("enrolment token", participantRegistrySiteEntity.getEnrollmentToken());
    templateArgs.put("contact email address", appPropertyConfig.getFromEmailAddress());
    EmailRequest emailRequest =
        new EmailRequest(
            appPropertyConfig.getFromEmailAddress(),
            new String[] {participantRegistrySiteEntity.getEmail()},
            null,
            null,
            appPropertyConfig.getParticipantInviteSubject(),
            appPropertyConfig.getParticipantInviteBody(),
            templateArgs);
    return emailService.sendMimeMail(emailRequest);
  }

  @Override
  @Transactional(readOnly = true)
  public SiteDetails getSites(String userId) {
    logger.entry("getSites(String userId)");

    List<SitePermissionEntity> sitePermissions =
        sitePermissionRepository.findSitePermissionByUserId(userId);

    if (CollectionUtils.isEmpty(sitePermissions)) {
      logger.exit(ErrorCode.SITE_NOT_FOUND);
      return new SiteDetails(ErrorCode.SITE_NOT_FOUND);
    }

    Map<String, StudyPermissionEntity> studyPermissionsByStudyInfoId =
        getStudyPermissionsByStudyId(userId, sitePermissions);

    List<String> usersSiteIds =
        sitePermissions
            .stream()
            .map(s -> s.getSite().getId())
            .distinct()
            .collect(Collectors.toList());

    Map<String, Long> siteWithInvitedParticipantCountMap =
        studyServiceImpl.getSiteWithInvitedParticipantCountMap(usersSiteIds);

    Map<String, Long> siteWithEnrolledParticipantCountMap =
        studyServiceImpl.getSiteWithEnrolledParticipantCountMap(usersSiteIds);

    Map<StudyEntity, List<SitePermissionEntity>> sitePermissionsByStudyId =
        sitePermissions.stream().collect(Collectors.groupingBy(SitePermissionEntity::getStudy));

    return prepareStudyWithSiteResponse(
        studyPermissionsByStudyInfoId,
        sitePermissionsByStudyId,
        siteWithInvitedParticipantCountMap,
        siteWithEnrolledParticipantCountMap);
  }

  private Map<String, StudyPermissionEntity> getStudyPermissionsByStudyId(
      String userId, List<SitePermissionEntity> sitePermissions) {
    List<String> usersStudyIds =
        sitePermissions
            .stream()
            .distinct()
            .map(studyEntity -> studyEntity.getStudy().getId())
            .collect(Collectors.toList());

    return studyServiceImpl.getStudyPermissionsByStudyInfoId(userId, usersStudyIds);
  }

  private SiteDetails prepareStudyWithSiteResponse(
      Map<String, StudyPermissionEntity> studyPermissionsByStudyInfoId,
      Map<StudyEntity, List<SitePermissionEntity>> sitePermissionsByStudyId,
      Map<String, Long> siteWithInvitedParticipantCountMap,
      Map<String, Long> siteWithEnrolledParticipantCountMap) {

    logger.entry("prepareStudyWithSiteResponse()");

    List<StudyDetails> studies = new ArrayList<>();

    for (Map.Entry<StudyEntity, List<SitePermissionEntity>> entry :
        sitePermissionsByStudyId.entrySet()) {

      StudyEntity study = entry.getKey();
      StudyDetails studyDetail = StudyMapper.setStudyDetails(studyPermissionsByStudyInfoId, study);

      studyDetail.setTotalSitesCount((long) entry.getValue().size());
      studies.add(studyDetail);

      List<Site> sites = new ArrayList<>();
      for (SitePermissionEntity sitePermission : entry.getValue()) {
        sites =
            getSitesList(
                siteWithInvitedParticipantCountMap,
                siteWithEnrolledParticipantCountMap,
                entry,
                sitePermission);
      }
      studyDetail.setSites(sites);
      studies.add(studyDetail);
    }
    logger.exit(MessageCode.GET_SITES_SUCCESS);
    return new SiteDetails(studies, MessageCode.GET_SITES_SUCCESS);
  }

  private static List<Site> getSitesList(
      Map<String, Long> siteWithInvitedParticipantCountMap,
      Map<String, Long> siteWithEnrolledParticipantCountMap,
      Map.Entry<StudyEntity, List<SitePermissionEntity>> entry,
      SitePermissionEntity sitePermission) {

    List<Site> sites = new ArrayList<>();
    Double percentage;
    Site site = new Site();
    site.setId(sitePermission.getSite().getId());
    site.setName(sitePermission.getSite().getLocation().getName());
    site.setEdit(sitePermission.getCanEdit());

    String studyType = entry.getKey().getType();
    if (studyType.equals(OPEN_STUDY)) {
      site.setInvited(Long.valueOf(sitePermission.getSite().getTargetEnrollment()));
    } else if (studyType.equals(CLOSE_STUDY)
        && siteWithInvitedParticipantCountMap.get(sitePermission.getSite().getId()) != null) {
      site.setInvited(siteWithInvitedParticipantCountMap.get(sitePermission.getSite().getId()));
    }

    if (siteWithEnrolledParticipantCountMap.get(sitePermission.getSite().getId()) != null) {
      site.setEnrolled(siteWithEnrolledParticipantCountMap.get(sitePermission.getSite().getId()));
    }

    if (site.getInvited() != 0 && site.getInvited() >= site.getEnrolled()) {
      percentage = (Double.valueOf(site.getEnrolled()) * 100) / Double.valueOf(site.getInvited());
      site.setEnrollmentPercentage(percentage);
    }

    sites.add(site);
    return sites;
  }

  @Override
  @Transactional(readOnly = true)
  public ParticipantDetailResponse getParticipantDetails(
      String participantRegistrySiteId, String userId) {
    logger.entry("begin getParticipantDetails()");

    Optional<ParticipantRegistrySiteEntity> optParticipantRegistry =
        participantRegistrySiteRepository.findById(participantRegistrySiteId);

    if (!optParticipantRegistry.isPresent()) {
      logger.exit(ErrorCode.GET_PARTICIPANTS_ERROR);
      return new ParticipantDetailResponse(ErrorCode.GET_PARTICIPANTS_ERROR);
    }

    Optional<SitePermissionEntity> sitePermissions =
        sitePermissionRepository.findSitePermissionByUserIdAndSiteId(
            userId, optParticipantRegistry.get().getSite().getId());

    if (!sitePermissions.isPresent()) {
      logger.exit(ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED);
      return new ParticipantDetailResponse(ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED);
    }

    ParticipantDetails participantDetails =
        ParticipantMapper.toParticipantDetailsResponse(optParticipantRegistry.get());

    List<ParticipantStudyEntity> participantsEnrollments =
        participantStudyRepository.findParticipantsEnrollment(participantRegistrySiteId);

    List<Enrollments> enrollmentList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(participantsEnrollments)) {
      List<String> participantStudyIds = new ArrayList<>();

      Enrollments enrollments =
          ParticipantMapper.toEnrollmentList(participantsEnrollments, participantStudyIds);

      enrollmentList.add(enrollments);
      participantDetails.setEnrollments(enrollmentList);

      List<StudyConsentEntity> studyConsents =
          studyConsentRepository.findByParticipantRegistrySiteId(participantStudyIds);

      List<ConsentHistory> consentHistories = ConsentMapper.toStudyConsents(studyConsents);

      participantDetails.setConsentHistory(consentHistories);
    } else {
      Enrollments enrollments = ParticipantMapper.toEnrollments();
      enrollmentList.add(enrollments);
      participantDetails.setEnrollments(enrollmentList);
    }
    logger.exit(MessageCode.GET_PARTICIPANT_DETAILS_SUCCESS);

    return new ParticipantDetailResponse(
        MessageCode.GET_PARTICIPANT_DETAILS_SUCCESS, participantDetails);
  }

  public ParticipantRegistryResponse getParticipants(
      String userId, String siteId, String onboardingStatus) {
    logger.info("getParticipants()");

    Optional<SiteEntity> optSite = siteRepository.findById(siteId);

    if (!optSite.isPresent()) {
      logger.exit(ErrorCode.SITE_NOT_FOUND);
      return new ParticipantRegistryResponse(ErrorCode.SITE_NOT_FOUND);
    }
    Optional<SitePermissionEntity> optSitePermission =
        sitePermissionRepository.findSitePermissionByUserIdAndSiteId(userId, siteId);

    // TODO (N) y can edit condition
    if (!optSitePermission.isPresent() || optSitePermission.get().getCanEdit() == 0) {
      logger.exit(ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED);
      return new ParticipantRegistryResponse(ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED);
    }

    ParticipantRegistryDetail participantRegistry =
        ParticipantMapper.fromSite(optSite.get(), optSitePermission.get());
    participantRegistry.setSiteId(siteId);

    setCountByStatus(siteId, participantRegistry);

    List<ParticipantRegistrySiteEntity> registryParticipants =
        participantRegistrySiteRepository.findParticipantRegistrySitesBySIteAndStatus(
            siteId, onboardingStatus);

    if (CollectionUtils.isNotEmpty(registryParticipants)) {
      List<ParticipantRequest> participants = new LinkedList<>();
      List<String> registryIds =
          registryParticipants
              .stream()
              .map(participant -> participant.getId())
              .collect(Collectors.toList());
      List<ParticipantStudyEntity> participantStudies =
          participantStudyRepository.findParticipantsByParticipantRegistrySite(registryIds);

      for (ParticipantRegistrySiteEntity participantRegistrySite : registryParticipants) {
        ParticipantRequest participant = new ParticipantRequest();
        setParticipant(participantStudies, participantRegistrySite, participant);

        if (participantRegistrySite.getInvitationDate() != null) {
          participant.setInvitedDate(
              DateTimeUtils.format(participantRegistrySite.getInvitationDate()));
        }
        participants.add(participant);
      }
      participantRegistry.setRegistryParticipants(participants);
    }

    ParticipantRegistryResponse participantRegistryResponse =
        new ParticipantRegistryResponse(
            MessageCode.GET_PARTICIPANT_REGISTRY_SUCCESS, participantRegistry);

    logger.exit(String.format("message=%s", participantRegistryResponse.getMessage()));
    return participantRegistryResponse;
  }

  private void setCountByStatus(String siteId, ParticipantRegistryDetail respBean) {
    List<Object[]> statusCount =
        participantRegistrySiteRepository.findParticipantRegistrySitesCountBySIteAndStatus(siteId);
    Map<String, Long> counts =
        statusCount.stream().collect(Collectors.toMap(a -> (String) a[0], a -> (Long) a[1]));
    Long allCount =
        counts.entrySet().stream().map(Map.Entry::getValue).reduce((long) 0, (a, b) -> a + b);
    counts.put("A", allCount);
    respBean.setCountByStatus(counts);
  }

  private void setParticipant(
      List<ParticipantStudyEntity> participantStudies,
      ParticipantRegistrySiteEntity participantRegistrySite,
      ParticipantRequest participant) {

    participant.setId(participantRegistrySite.getId());
    participant.setEmail(participantRegistrySite.getEmail());
    String onboardingStatusCode = participantRegistrySite.getOnboardingStatus();
    participant.setOnboardingStatus(OnboardingStatus.fromCode(onboardingStatusCode).getStatus());
    Map<String, ParticipantStudyEntity> idMap = new HashMap<>();
    for (ParticipantStudyEntity participantStudy : participantStudies) {
      if (participantStudy.getParticipantRegistrySite() != null) {
        idMap.put(participantStudy.getParticipantRegistrySite().getId(), participantStudy);
      }
    }

    ParticipantStudyEntity participantStudy = idMap.get(participantRegistrySite.getId());
    if (participantStudy != null) {
      participant.setEnrollmentStatus(participantStudy.getStatus());
      if (participantStudy.getEnrolledDate() != null) {
        participant.setEnrollmentDate(DateTimeUtils.format(participantStudy.getEnrolledDate()));
      }
    } else {
      if (OnboardingStatus.NEW.getCode().equals(onboardingStatusCode)
          || OnboardingStatus.INVITED.getCode().equals(onboardingStatusCode)) {
        participant.setEnrollmentStatus(CommonConstants.YET_TO_ENROLL);
      }
    }
  }
}
