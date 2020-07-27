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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.cloud.healthcare.fdamystudies.beans.ConsentHistory;
import com.google.cloud.healthcare.fdamystudies.beans.EmailRequest;
import com.google.cloud.healthcare.fdamystudies.beans.EmailResponse;
import com.google.cloud.healthcare.fdamystudies.beans.EnableDisableParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.EnableDisableParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.Enrollments;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetail;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetailRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetailResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetails;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryDetail;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.Site;
import com.google.cloud.healthcare.fdamystudies.beans.SiteDetailsResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SiteResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteStatusResponse;
import com.google.cloud.healthcare.fdamystudies.beans.StudyDetails;
import com.google.cloud.healthcare.fdamystudies.common.CommonConstants;
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
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteCount;
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

  private boolean isEditPermissionAllowed(String studyId, String userId) {
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
  public SiteStatusResponse toggleSiteStatus(String userId, String siteId) {
    logger.entry("toggleSiteStatus()");

    ErrorCode errorCode = validateDecommissionSiteRequest(userId, siteId);
    if (errorCode != null) {
      logger.exit(errorCode);
      return new SiteStatusResponse(errorCode);
    }

    Optional<SiteEntity> optSiteEntity = siteRepository.findById(siteId);

    SiteEntity site = optSiteEntity.get();
    if (SiteStatus.DEACTIVE == SiteStatus.fromValue(site.getStatus())) {
      site.setStatus(SiteStatus.ACTIVE.value());
      site = siteRepository.saveAndFlush(site);

      logger.exit(String.format(" Site status changed to ACTIVE for siteId=%s", site.getId()));
      return new SiteStatusResponse(
          site.getId(), site.getStatus(), MessageCode.RECOMMISSION_SITE_SUCCESS);
    }

    site.setStatus(SiteStatus.DEACTIVE.value());
    siteRepository.saveAndFlush(site);
    setPermissions(siteId);

    logger.exit(String.format("Site status changed to DEACTIVE for siteId=%s", site.getId()));
    return new SiteStatusResponse(
        site.getId(), site.getStatus(), MessageCode.DECOMMISSION_SITE_SUCCESS);
  }

  private ErrorCode validateDecommissionSiteRequest(String userId, String siteId) {
    Optional<SitePermissionEntity> optSitePermission =
        sitePermissionRepository.findSitePermissionByUserIdAndSiteId(userId, siteId);
    if (!optSitePermission.isPresent()) {
      return ErrorCode.SITE_NOT_FOUND;
    }

    SitePermissionEntity sitePermission = optSitePermission.get();
    if (OPEN.equalsIgnoreCase(sitePermission.getStudy().getType())) {
      return ErrorCode.CANNOT_DECOMMISSION_SITE_FOR_OPEN_STUDY;
    }

    String studyId = sitePermission.getStudy().getId();
    boolean canEdit = isEditPermissionAllowed(studyId, userId);
    if (!canEdit) {
      return ErrorCode.SITE_PERMISSION_ACEESS_DENIED;
    }

    List<String> status = Arrays.asList(ENROLLED_STATUS, STATUS_ACTIVE);
    Optional<Long> optParticipantStudyCount =
        participantStudyRepository.findByStudyIdAndStatus(status, studyId);

    if (optParticipantStudyCount.isPresent() && optParticipantStudyCount.get() > 0) {
      return ErrorCode.CANNOT_DECOMMISSION_SITE_FOR_ENROLLED_ACTIVE_STATUS;
    }

    return null;
  }

  private void setPermissions(String siteId) {

    List<SitePermissionEntity> sitePermissions =
        (List<SitePermissionEntity>)
            CollectionUtils.emptyIfNull(sitePermissionRepository.findBySiteId(siteId));

    List<String> studyIds =
        sitePermissions
            .stream()
            .distinct()
            .map(studyId -> studyId.getStudy().getId())
            .collect(Collectors.toList());

    List<String> siteAdminIds =
        sitePermissions
            .stream()
            .distinct()
            .map(urAdminId -> urAdminId.getUrAdminUser().getId())
            .collect(Collectors.toList());

    List<StudyPermissionEntity> studyPermissions =
        (List<StudyPermissionEntity>)
            CollectionUtils.emptyIfNull(
                studyPermissionRepository.findByByUserIdsAndStudyIds(siteAdminIds, studyIds));

    List<String> studyAdminIds =
        studyPermissions
            .stream()
            .distinct()
            .map(studyAdminId -> studyAdminId.getUrAdminUser().getId())
            .collect(Collectors.toList());

    for (SitePermissionEntity sitePermission : sitePermissions) {
      if (studyAdminIds.contains(sitePermission.getUrAdminUser().getId())) {
        sitePermission.setCanEdit(Permission.READ_VIEW.value());
        sitePermissionRepository.saveAndFlush(sitePermission);
      } else {
        sitePermissionRepository.delete(sitePermission);
      }
    }
    deactivateYetToEnrollParticipants(siteId);
  }

  private void deactivateYetToEnrollParticipants(String siteId) {
    List<ParticipantStudyEntity> participantStudies =
        (List<ParticipantStudyEntity>)
            CollectionUtils.emptyIfNull(
                participantStudyRepository.findBySiteIdAndStatus(siteId, YET_TO_JOIN));

    List<String> participantRegistrySiteIds =
        participantStudies
            .stream()
            .distinct()
            .map(participantStudy -> participantStudy.getParticipantRegistrySite().getId())
            .collect(Collectors.toList());

    List<ParticipantRegistrySiteEntity> participantRegistrySites =
        participantRegistrySiteRepository.findByIds(participantRegistrySiteIds);

    for (ParticipantRegistrySiteEntity participantRegistrySite :
        CollectionUtils.emptyIfNull(participantRegistrySites)) {
      participantRegistrySite.setOnboardingStatus(OnboardingStatus.DISABLED.getCode());
      participantRegistrySiteRepository.saveAndFlush(participantRegistrySite);
    }
  }

  @Override
  @Transactional
  public ParticipantResponse addNewParticipant(
      ParticipantDetailRequest participant, String userId) {
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
      ParticipantDetailRequest participant, String userId, SiteEntity site) {
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
        participantRegistrySiteRepository.findByStudyIdAndEmail(
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
        participantRegistrySiteRepository.findByIds(inviteParticipantRequest.getIds());

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
  public SiteDetailsResponse getSites(String userId) {
    logger.entry("getSites(String userId)");

    List<SitePermissionEntity> sitePermissions =
        sitePermissionRepository.findSitePermissionByUserId(userId);
    if (CollectionUtils.isEmpty(sitePermissions)) {
      logger.exit(ErrorCode.SITE_NOT_FOUND);
      return new SiteDetailsResponse(ErrorCode.SITE_NOT_FOUND);
    }

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

    Map<StudyEntity, List<SitePermissionEntity>> sitePermissionsByStudyId =
        sitePermissions.stream().collect(Collectors.groupingBy(SitePermissionEntity::getStudy));

    Map<String, StudyPermissionEntity> studyPermissionsByStudyInfoId =
        getStudyPermissionsByStudyId(userId, sitePermissions);

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

    return getStudyPermissionsByStudyInfoId(userId, usersStudyIds);
  }

  private Map<String, Long> getSiteWithInvitedParticipantCountMap(List<String> usersSiteIds) {
    List<ParticipantRegistrySiteEntity> participantRegistry =
        participantRegistrySiteRepository.findBySiteIds(usersSiteIds);

    return participantRegistry
        .stream()
        .collect(
            Collectors.groupingBy(
                e -> e.getSite().getId(),
                Collectors.summingLong(ParticipantRegistrySiteEntity::getInvitationCount)));
  }

  private Map<String, Long> getSiteWithEnrolledParticipantCountMap(List<String> usersSiteIds) {
    List<ParticipantStudyEntity> participantsEnrollments =
        participantStudyRepository.findBySiteIds(usersSiteIds);

    return participantsEnrollments
        .stream()
        .collect(Collectors.groupingBy(e -> e.getSite().getId(), Collectors.counting()));
  }

  private Map<String, StudyPermissionEntity> getStudyPermissionsByStudyInfoId(
      String userId, List<String> usersStudyIds) {
    List<StudyPermissionEntity> studyPermissions =
        studyPermissionRepository.findByStudyIds(usersStudyIds, userId);

    Map<String, StudyPermissionEntity> studyPermissionsByStudyInfoId = new HashMap<>();
    if (CollectionUtils.isNotEmpty(studyPermissions)) {
      studyPermissionsByStudyInfoId =
          studyPermissions
              .stream()
              .collect(Collectors.toMap(e -> e.getStudy().getId(), Function.identity()));
    }
    return studyPermissionsByStudyInfoId;
  }

  private SiteDetailsResponse prepareStudyWithSiteResponse(
      Map<String, StudyPermissionEntity> studyPermissionsByStudyInfoId,
      Map<StudyEntity, List<SitePermissionEntity>> sitePermissionsByStudyId,
      Map<String, Long> siteWithInvitedParticipantCountMap,
      Map<String, Long> siteWithEnrolledParticipantCountMap) {
    logger.entry("prepareStudyWithSiteResponse()");

    List<StudyDetails> studies = new ArrayList<>();
    for (Map.Entry<StudyEntity, List<SitePermissionEntity>> entry :
        sitePermissionsByStudyId.entrySet()) {
      StudyEntity study = entry.getKey();
      StudyDetails studyDetail = StudyMapper.toStudyDetails(studyPermissionsByStudyInfoId, study);
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
    return new SiteDetailsResponse(studies, MessageCode.GET_SITES_SUCCESS);
  }

  private static List<Site> getSitesList(
      Map<String, Long> siteWithInvitedParticipantCountMap,
      Map<String, Long> siteWithEnrolledParticipantCountMap,
      Map.Entry<StudyEntity, List<SitePermissionEntity>> entry,
      SitePermissionEntity sitePermission) {

    List<Site> sites = new ArrayList<>();
    Site site = new Site();
    site.setId(sitePermission.getSite().getId());
    site.setName(sitePermission.getSite().getLocation().getName());
    site.setEdit(sitePermission.getCanEdit());

    calucalateEnrollmentPercentageForSite(
        siteWithInvitedParticipantCountMap,
        siteWithEnrolledParticipantCountMap,
        entry,
        sitePermission,
        site);

    sites.add(site);
    return sites;
  }

  private static void calucalateEnrollmentPercentageForSite(
      Map<String, Long> siteWithInvitedParticipantCountMap,
      Map<String, Long> siteWithEnrolledParticipantCountMap,
      Map.Entry<StudyEntity, List<SitePermissionEntity>> entry,
      SitePermissionEntity sitePermission,
      Site site) {
    Double percentage;
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

  @Override
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

    if (!optSitePermission.isPresent()
        || Permission.NO_PERMISSION == Permission.fromValue(optSitePermission.get().getCanEdit())) {
      logger.exit(ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED);
      return new ParticipantRegistryResponse(ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED);
    }

    ParticipantRegistryDetail participantRegistry =
        ParticipantMapper.fromSite(optSite.get(), optSitePermission.get(), siteId);
    Map<String, Long> statusWithCountMap = getOnboardingStatusWithCount(siteId);
    participantRegistry.setCountByStatus(statusWithCountMap);

    List<ParticipantRegistrySiteEntity> registryParticipants = null;
    if (StringUtils.isEmpty(onboardingStatus)) {
      registryParticipants = participantRegistrySiteRepository.findBySiteId(siteId);
    } else {
      registryParticipants =
          participantRegistrySiteRepository.findBySiteIdAndStatus(siteId, onboardingStatus);
    }

    addRegistryParticipants(participantRegistry, registryParticipants);

    ParticipantRegistryResponse participantRegistryResponse =
        new ParticipantRegistryResponse(
            MessageCode.GET_PARTICIPANT_REGISTRY_SUCCESS, participantRegistry);

    logger.exit(String.format("message=%s", participantRegistryResponse.getMessage()));
    return participantRegistryResponse;
  }

  private void addRegistryParticipants(
      ParticipantRegistryDetail participantRegistry,
      List<ParticipantRegistrySiteEntity> registryParticipants) {
    List<String> registryIds =
        CollectionUtils.emptyIfNull(registryParticipants)
            .stream()
            .map(ParticipantRegistrySiteEntity::getId)
            .collect(Collectors.toList());

    List<ParticipantStudyEntity> participantStudies =
        (List<ParticipantStudyEntity>)
            CollectionUtils.emptyIfNull(
                participantStudyRepository.findParticipantsByParticipantRegistrySite(registryIds));

    for (ParticipantRegistrySiteEntity participantRegistrySite : registryParticipants) {
      ParticipantDetail participant = new ParticipantDetail();
      participant =
          ParticipantMapper.toParticipantDetails(
              participantStudies, participantRegistrySite, participant);
      participantRegistry.getRegistryParticipants().add(participant);
    }
  }

  private Map<String, Long> getOnboardingStatusWithCount(String siteId) {
    List<ParticipantRegistrySiteCount> statusCount =
        (List<ParticipantRegistrySiteCount>)
            CollectionUtils.emptyIfNull(
                participantRegistrySiteRepository.findParticipantRegistrySitesCountBySiteId(
                    siteId));

    Map<String, Long> statusWithCountMap = new HashMap<>();
    for (OnboardingStatus onboardingStatus : OnboardingStatus.values()) {
      statusWithCountMap.put(onboardingStatus.getCode(), (long) 0);
    }

    long total = 0;
    for (ParticipantRegistrySiteCount count : statusCount) {
      total += count.getCount();
      statusWithCountMap.put(count.getOnboardingStatus(), count.getCount());
    }

    statusWithCountMap.put("A", total);
    return statusWithCountMap;
  }

  /*@Override
  public ParticipantRegistryResponse importParticipant(
      String userId, String siteId, MultipartFile multipartFile) {
    try {
      Workbook workbook =
          WorkbookFactory.create(new BufferedInputStream(multipartFile.getInputStream()));
      ParticipantRegistryResponse respBean = new ParticipantRegistryResponse();
      Sheet sheet = workbook.getSheetAt(0);
      Row row = sheet.getRow(0);
      String columnName = row.getCell(1).getStringCellValue();
      if (!"Email Address".equalsIgnoreCase(columnName)) {
        return new ParticipantRegistryResponse(ErrorCode.DOCUMENT_NOT_IN_PRESCRIBED_FORMAT);
      }
      Iterator<Row> it = sheet.rowIterator();
      Set<String> invalidEmails = new HashSet<>();
      List<ParticipantDetail> participants = new LinkedList<>();
      while (it.hasNext()) {
        Row r = it.next();
        if (r.getRowNum() == 0) {
          continue;
        }
        String email = null;
        try {
          email = r.getCell(1).getStringCellValue();
          if (!StringUtils.isBlank(email) && Pattern.matches(EMAIL_REGEX, email)) {
            ParticipantDetail participant = new ParticipantDetail();
            participant.setEmail(email);
            participant.setSiteId(siteId);
            participants.add(participant);
          } else {
            invalidEmails.add(email);
          }
        } catch (Exception e) {
          invalidEmails.add(email);
          continue;
        }
      }
    } catch (EncryptedDocumentException | InvalidFormatException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }*/

  /* public ConsentDocument getConsentDocument(String consentId, String userId) {
    logger.entry("begin getConsentDocument(consentId,userId)");
    ConsentDocument consentDocument = new ConsentDocument();
    Optional<StudyConsentEntity> optStudyConsent =
        studyConsentRepository.findByConsentId(consentId);
    StudyConsentEntity studyConsentEntity = optStudyConsent.get();
    if (studyConsentEntity != null
        && studyConsentEntity.getParticipantStudy() != null
        && studyConsentEntity.getParticipantStudy().getSite() != null
        && studyConsentEntity.getParticipantStudy().getSite().getId() != null) {
      Optional<SitePermissionEntity> optSitePermission =
          sitePermissionRepository.findSitePermissionByUserIdAndSiteId(
              userId, studyConsentEntity.getParticipantStudy().getSite().getId());
      if (!optSitePermission.isPresent()) {
        logger.exit(ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED);
        return new ConsentDocument(ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED);
      }
      if (studyConsentEntity.getPdfStorage() == 1) {
        String path = studyConsentEntity.getPdfPath();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cloudStorageService.downloadFileTo(path, baos);
        consentDocument.setContent(new String(baos.toByteArray()));
      }
      consentDocument.setType("application/pdf");
    } else {
      logger.exit(ErrorCode.ERROR_GETTING_CONSENT_DATA);
      return new ConsentDocument(ErrorCode.ERROR_GETTING_CONSENT_DATA);
    }
    ConsentDocument consentDocumentResponse =
        new ConsentDocument(MessageCode.GET_CONSENT_DOCUMENT_SUCCESS);
    return consentDocumentResponse;
  }*/

  public EnableDisableParticipantResponse updateOnboardingStatus(
      EnableDisableParticipantRequest bean, String siteId, String userId) {
    logger.entry("begin updateOnboardingStatus()");
    Optional<SiteEntity> optSite = siteRepository.findById(siteId);

    if (!optSite.isPresent() || !optSite.get().getStatus().equals(ACTIVE_STATUS)) {
      logger.exit(ErrorCode.SITE_NOT_EXIST_OR_INACTIVE);
      return new EnableDisableParticipantResponse(ErrorCode.SITE_NOT_EXIST_OR_INACTIVE);
    }
    Optional<SitePermissionEntity> optSitePermission =
        sitePermissionRepository.findSitePermissionByUserIdAndSiteId(userId, siteId);

    if (!optSitePermission.isPresent()
        || !optSitePermission.get().getCanEdit().equals(Permission.READ_EDIT.value())) {
      logger.exit(ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED);
      return new EnableDisableParticipantResponse(ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED);
    }
    List<ParticipantRegistrySiteEntity> list =
        participantRegistrySiteRepository.findByIds(bean.getId());
    List<String> ids = new ArrayList<>();
    if (ACTIVE_STATUS.equals(bean.getStatus())) {
      for (ParticipantRegistrySiteEntity part : list) {
        updateStatusToNew(optSite, ids, part);
      }
      // TODO (N) if ids.size(0) error code?
      return prepareResponse(ids, OnboardingStatus.NEW.getCode());
    } else {
      for (ParticipantRegistrySiteEntity part : list) {
        ids.add(part.getId());
      }
      return prepareResponse(ids, OnboardingStatus.DISABLED.getCode());
    }
  }

  private EnableDisableParticipantResponse prepareResponse(
      List<String> ids, String onboardingStatus) {
    ParticipantRegistrySiteEntity participantRegistrySiteEntity =
        new ParticipantRegistrySiteEntity();
    for (String id : ids) {
      participantRegistrySiteEntity.setOnboardingStatus(onboardingStatus);
      participantRegistrySiteEntity.setId(id);
      participantRegistrySiteRepository.saveAndFlush(participantRegistrySiteEntity);
    }
    if (onboardingStatus.equals(OnboardingStatus.NEW.getCode())) {
      return new EnableDisableParticipantResponse(MessageCode.PARTICIPANT_ENABLED);
    } else {
      return new EnableDisableParticipantResponse(MessageCode.PARTICIPANT_DISABLED);
    }
  }

  private void updateStatusToNew(
      Optional<SiteEntity> optSite, List<String> ids, ParticipantRegistrySiteEntity part) {
    // TODO(N) chk with old code
    List<ParticipantRegistrySiteEntity> existing =
        participantRegistrySiteRepository.findByStudyIdAndEmail1(
            optSite.get().getStudy().getId(), part.getEmail());

    if (CollectionUtils.isEmpty(existing)) {
      ids.add(part.getId());
    } else {
      boolean existingNewInvited = false;
      for (ParticipantRegistrySiteEntity exist : existing) {
        if (OnboardingStatus.NEW.getCode().equals(exist.getOnboardingStatus())
            || OnboardingStatus.INVITED.getCode().equals(exist.getOnboardingStatus())) {
          existingNewInvited = true;
          break;
        }
      }
      if (!existingNewInvited) {
        ids.add(part.getId());
      }
    }
  }
}
