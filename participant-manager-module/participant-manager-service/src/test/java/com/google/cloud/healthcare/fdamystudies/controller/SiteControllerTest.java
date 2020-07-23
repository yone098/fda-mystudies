/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.controller;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.ENROLLED_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.USER_ID_HEADER;
import static com.google.cloud.healthcare.fdamystudies.common.ErrorCode.EMAIL_EXISTS;
import static com.google.cloud.healthcare.fdamystudies.common.ErrorCode.ENROLLED_PARTICIPANT;
import static com.google.cloud.healthcare.fdamystudies.common.ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED;
import static com.google.cloud.healthcare.fdamystudies.common.ErrorCode.OPEN_STUDY;
import static com.google.cloud.healthcare.fdamystudies.common.ErrorCode.SITE_NOT_EXIST_OR_INACTIVE;
import static com.google.cloud.healthcare.fdamystudies.common.ErrorCode.SITE_NOT_FOUND;
import static com.google.cloud.healthcare.fdamystudies.common.JsonUtils.readJsonFile;
import static com.google.cloud.healthcare.fdamystudies.common.TestConstants.DECOMMISSION_SITE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SiteRequest;
import com.google.cloud.healthcare.fdamystudies.common.ApiEndpoint;
import com.google.cloud.healthcare.fdamystudies.common.BaseMockIT;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.IdGenerator;
import com.google.cloud.healthcare.fdamystudies.common.JsonUtils;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.common.OnboardingStatus;
import com.google.cloud.healthcare.fdamystudies.common.Permission;
import com.google.cloud.healthcare.fdamystudies.common.SiteStatus;
import com.google.cloud.healthcare.fdamystudies.helper.TestDataHelper;
import com.google.cloud.healthcare.fdamystudies.model.AppEntity;
import com.google.cloud.healthcare.fdamystudies.model.AppPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.LocationEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.SitePermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyConsentEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantRegistrySiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantStudyRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyConsentRepository;
import com.google.cloud.healthcare.fdamystudies.service.SiteService;
import com.google.cloud.healthcare.fdamystudies.util.Constants;
import com.jayway.jsonpath.JsonPath;

public class SiteControllerTest extends BaseMockIT {

  private static String siteId;

  @Autowired private SiteController controller;

  @Autowired private SiteService siteService;

  @Autowired private TestDataHelper testDataHelper;

  @Autowired private SiteRepository siteRepository;

  @Autowired private ParticipantRegistrySiteRepository participantRegistrySiteRepository;

  @Autowired private ParticipantStudyRepository participantStudyRepository;

  @Autowired private StudyConsentRepository studyConsentRepository;

  protected MvcResult result;

  private UserRegAdminEntity userRegAdminEntity;

  private StudyEntity studyEntity;

  private LocationEntity locationEntity;

  private AppEntity appEntity;

  private SiteEntity siteEntity;

  private SitePermissionEntity sitePermissionEntity;

  private ParticipantRegistrySiteEntity participantRegistrySiteEntity;

  private ParticipantStudyEntity participantStudyEntity;

  private StudyConsentEntity studyConsentEntity;

  @BeforeEach
  public void setUp() {
    locationEntity = testDataHelper.createLocation();
    userRegAdminEntity = testDataHelper.createUserRegAdminEntity();
    appEntity = testDataHelper.createAppEntity(userRegAdminEntity);
    studyEntity = testDataHelper.createStudyEntity(userRegAdminEntity, appEntity);
    siteEntity = testDataHelper.createSiteEntity(studyEntity, userRegAdminEntity, appEntity);
    participantRegistrySiteEntity =
        testDataHelper.createParticipantRegistrySite(siteEntity, studyEntity);
    participantStudyEntity =
        testDataHelper.createParticipantStudyEntity(
            siteEntity, studyEntity, participantRegistrySiteEntity);
    studyConsentEntity = testDataHelper.createStudyConsentEntity(participantStudyEntity);
  }

  @Test
  public void contextLoads() {
    assertNotNull(controller);
    assertNotNull(mockMvc);
    assertNotNull(siteService);
  }

  @Test
  public void shouldReturnBadRequestForAddNewSite() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    SiteRequest siteRequest = new SiteRequest();
    MvcResult result =
        mockMvc
            .perform(
                post(ApiEndpoint.ADD_NEW_SITE.getPath())
                    .content(JsonUtils.asJsonString(siteRequest))
                    .headers(headers)
                    .contextPath(getContextPath()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.violations").isArray())
            .andReturn();

    String actualResponse = result.getResponse().getContentAsString();
    String expectedResponse = readJsonFile("/responses/add_site_bad_request.json");
    JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.NON_EXTENSIBLE);
  }

  @Test
  public void shouldReturnSitePermissionAccessDeniedForAddNewSite() throws Exception {
    // pre-condition: deny study permission
    StudyPermissionEntity studyPermissionEntity = studyEntity.getStudyPermissions().get(0);
    studyPermissionEntity.setEdit(Permission.READ_VIEW.value());
    studyEntity = testDataHelper.getStudyRepository().saveAndFlush(studyEntity);

    // pre-condition: deny app permission
    AppPermissionEntity appPermissionEntity = appEntity.getAppPermissions().get(0);
    appPermissionEntity.setEdit(Permission.READ_VIEW.value());
    appEntity = testDataHelper.getAppRepository().saveAndFlush(appEntity);
    HttpHeaders headers = newCommonHeaders();
    SiteRequest siteRequest = newSiteRequest();

    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_SITE.getPath())
                .content(JsonUtils.asJsonString(siteRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(
            jsonPath(
                "$.error_description",
                is(ErrorCode.SITE_PERMISSION_ACEESS_DENIED.getDescription())));
  }

  @Test
  public void shouldAddNewSite() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    SiteRequest siteRequest = newSiteRequest();
    MvcResult result =
        mockMvc
            .perform(
                post(ApiEndpoint.ADD_NEW_SITE.getPath())
                    .content(JsonUtils.asJsonString(siteRequest))
                    .headers(headers)
                    .contextPath(getContextPath()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.siteId", notNullValue()))
            .andExpect(jsonPath("$.message", is(MessageCode.ADD_SITE_SUCCESS.getMessage())))
            .andReturn();

    siteId = JsonPath.read(result.getResponse().getContentAsString(), "$.siteId");

    // verify updated values
    Optional<SiteEntity> optSiteEntity = siteRepository.findById(siteId);
    SiteEntity siteEntity = optSiteEntity.get();
    SitePermissionEntity sitePermissionEntity = siteEntity.getSitePermissions().get(0);
    assertNotNull(siteEntity);
    assertEquals(locationEntity.getId(), siteEntity.getLocation().getId());
    assertEquals(sitePermissionEntity.getCreatedBy(), userRegAdminEntity.getId());
  }

  @Test
  public void shouldReturnNotFoundForDecomissionSite() throws Exception {
    HttpHeaders headers = newCommonHeaders();

    // call API to return SITE_NOT_FOUND error_description
    mockMvc
        .perform(
            put(ApiEndpoint.DECOMISSION_SITE.getPath(), IdGenerator.id())
                .headers(headers)
                .contextPath(getContextPath()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error_description", is(ErrorCode.SITE_NOT_FOUND.getDescription())));
  }

  @Test
  public void shouldRecomissionSite() throws Exception {
    HttpHeaders headers = newCommonHeaders();

    // Step 1: Set the status to DEACTIVE
    siteEntity.setStatus(SiteStatus.DEACTIVE.value());
    siteEntity = testDataHelper.getSiteRepository().saveAndFlush(siteEntity);

    // Step 2: call API to return RECOMMISSION_SITE_SUCCESS
    result =
        mockMvc
            .perform(
                put(ApiEndpoint.DECOMISSION_SITE.getPath(), siteEntity.getId())
                    .headers(headers)
                    .contextPath(getContextPath()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.siteId", notNullValue()))
            .andExpect(
                jsonPath("$.message", is(MessageCode.RECOMMISSION_SITE_SUCCESS.getMessage())))
            .andExpect(jsonPath("$.status", is(SiteStatus.ACTIVE.value())))
            .andReturn();

    String siteId = JsonPath.read(result.getResponse().getContentAsString(), "$.siteId");

    // Step 3: verify updated values
    Optional<SiteEntity> optSiteEntity = siteRepository.findById(siteId);
    SiteEntity siteEntity = optSiteEntity.get();
    assertNotNull(siteEntity);
    assertEquals(DECOMMISSION_SITE_NAME, siteEntity.getName());
  }

  @Test
  public void shouldDecomissionSite() throws Exception {
    HttpHeaders headers = newCommonHeaders();

    // Step 1: set status to ACTIVE
    siteEntity.setStatus(SiteStatus.ACTIVE.value());
    siteEntity = testDataHelper.getSiteRepository().saveAndFlush(siteEntity);

    // Step 2: Call API to return DECOMMISSION_SITE_SUCCESS message
    result =
        mockMvc
            .perform(
                put(ApiEndpoint.DECOMISSION_SITE.getPath(), siteEntity.getId())
                    .headers(headers)
                    .contextPath(getContextPath()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.siteId", notNullValue()))
            .andExpect(jsonPath("$.status", is(SiteStatus.DEACTIVE.value())))
            .andExpect(
                jsonPath("$.message", is(MessageCode.DECOMMISSION_SITE_SUCCESS.getMessage())))
            .andReturn();

    String siteId = JsonPath.read(result.getResponse().getContentAsString(), "$.siteId");

    // Step 3: verify updated values
    Optional<SiteEntity> optSiteEntity = siteRepository.findById(siteId);
    SiteEntity siteEntity = optSiteEntity.get();
    assertNotNull(siteEntity);
    assertEquals(DECOMMISSION_SITE_NAME, siteEntity.getName());
  }

  @Test
  public void shouldReturnOpenStudyForDecomissionSite() throws Exception {
    HttpHeaders headers = newCommonHeaders();

    // Step 1: set studyType to open
    studyEntity.setType("Open");
    studyEntity = testDataHelper.getStudyRepository().saveAndFlush(studyEntity);

    // Step 2: call API to return OPEN_STUDY_FOR_DECOMMISSION_SITE error_description
    mockMvc
        .perform(
            put(ApiEndpoint.DECOMISSION_SITE.getPath(), siteEntity.getId())
                .headers(headers)
                .contextPath(getContextPath()))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath(
                "$.error_description",
                is(ErrorCode.OPEN_STUDY_FOR_DECOMMISSION_SITE.getDescription())));
  }

  @Test
  public void shouldReturnBadRequestForAddNewParticipant() throws Exception {
    HttpHeaders headers = newCommonHeaders();

    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_PARTICIPANT.getPath(), IdGenerator.id())
                .headers(headers)
                .content(JsonUtils.asJsonString(newParticipantRequest()))
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error_description", is(SITE_NOT_EXIST_OR_INACTIVE.getDescription())));
  }

  @Test
  public void shouldReturnBadRequestForNewParticipant() throws Exception {
    // Step 1: set participantStudy status to enrolled
    siteEntity.setStudy(studyEntity);
    participantStudyEntity.setStatus(ENROLLED_STATUS);
    participantRegistrySiteEntity.setEmail(newParticipantRequest().getEmail());
    participantStudyRepository.saveAndFlush(participantStudyEntity);

    // Step 2: Call API to return ENROLLED_PARTICIPANT errorDescription
    HttpHeaders headers = newCommonHeaders();
    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_PARTICIPANT.getPath(), siteEntity.getId())
                .headers(headers)
                .content(JsonUtils.asJsonString(newParticipantRequest()))
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error_description", is(ENROLLED_PARTICIPANT.getDescription())));
  }

  @Test
  public void shouldReturnEmailExistForAddNewParticipant() throws Exception {
    // Step 1: set emailId
    siteEntity.setStudy(studyEntity);
    participantRegistrySiteEntity.setEmail(newParticipantRequest().getEmail());
    participantRegistrySiteRepository.saveAndFlush(participantRegistrySiteEntity);

    // Step 2: Call API to return EMAIL_EXISTS errorDescription
    HttpHeaders headers = newCommonHeaders();
    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_PARTICIPANT.getPath(), siteEntity.getId())
                .headers(headers)
                .content(JsonUtils.asJsonString(newParticipantRequest()))
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error_description", is(EMAIL_EXISTS.getDescription())));
  }

  @Test
  public void shouldReturnForbiddenForAddNewParticipant() throws Exception {
    // Step 1: set manage site permission to view only
    sitePermissionEntity = siteEntity.getSitePermissions().get(0);
    sitePermissionEntity.setCanEdit(Permission.READ_VIEW.value());
    testDataHelper.getSiteRepository().saveAndFlush(siteEntity);

    // Step 2: Call API to return MANAGE_SITE_PERMISSION_ACCESS_DENIED errorDescription
    HttpHeaders headers = newCommonHeaders();
    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_PARTICIPANT.getPath(), siteEntity.getId())
                .headers(headers)
                .content(JsonUtils.asJsonString(newParticipantRequest()))
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(
            jsonPath(
                "$.error_description", is(MANAGE_SITE_PERMISSION_ACCESS_DENIED.getDescription())));
  }

  @Test
  public void shouldReturnForbiddenForNewParticipant() throws Exception {
    // Step 1: set study type to open study
    sitePermissionEntity = siteEntity.getSitePermissions().get(0);
    // sitePermissionEntity.setCanEdit(Permission.READ_EDIT.value());
    studyEntity.setType("OPEN");
    siteEntity.setStudy(studyEntity);
    testDataHelper.getSiteRepository().saveAndFlush(siteEntity);

    // Step 2: Call API to return OPEN_STUDY errorDescription
    HttpHeaders headers = newCommonHeaders();
    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_PARTICIPANT.getPath(), siteEntity.getId())
                .headers(headers)
                .content(JsonUtils.asJsonString(newParticipantRequest()))
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error_description", is(OPEN_STUDY.getDescription())));
  }

  @Test
  public void shouldAddNewParticipant() throws Exception {
    // Step 1: Set studyEntity
    siteEntity.setStudy(studyEntity);
    testDataHelper.getSiteRepository().saveAndFlush(siteEntity);
    ParticipantRequest participantRequest = newParticipantRequest();

    // Step 2: Call API to get ADD_PARTICIPANT_SUCCESS
    HttpHeaders headers = newCommonHeaders();
    MvcResult result =
        mockMvc
            .perform(
                post(ApiEndpoint.ADD_NEW_PARTICIPANT.getPath(), siteEntity.getId())
                    .content(JsonUtils.asJsonString(participantRequest))
                    .headers(headers)
                    .contextPath(getContextPath()))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.participantId", notNullValue()))
            .andExpect(jsonPath("$.message", is(MessageCode.ADD_PARTICIPANT_SUCCESS.getMessage())))
            .andReturn();

    String participantId =
        JsonPath.read(result.getResponse().getContentAsString(), "$.participantId");

    // Step 3: verify saved values
    Optional<ParticipantRegistrySiteEntity> optParticipantRegistrySite =
        participantRegistrySiteRepository.findById(participantId);
    assertNotNull(optParticipantRegistrySite.get().getSite());
    assertEquals(siteEntity.getId(), optParticipantRegistrySite.get().getSite().getId());
    assertEquals(participantRequest.getEmail(), optParticipantRegistrySite.get().getEmail());
  }

  @Test
  public void shouldReturnStudiesWithSites() throws Exception {

    // Step 1: set the data needed to get studies with sites
    studyEntity.setAppInfo(appEntity);
    siteEntity.setLocation(locationEntity);
    participantRegistrySiteEntity.setEmail(TestDataHelper.EMAIL_VALUE);
    testDataHelper.getSiteRepository().save(siteEntity);
    testDataHelper.getParticipantRegistrySiteRepository().save(participantRegistrySiteEntity);

    HttpHeaders headers = newCommonHeaders();
    // Step 2: call API to return GET_SITES_SUCCESS message
    mockMvc
        .perform(
            get(ApiEndpoint.GET_SITES.getPath()).headers(headers).contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studies").isArray())
        .andExpect(jsonPath("$.studies[0].id").isNotEmpty())
        .andExpect(jsonPath("$.studies[0].totalSitesCount").value(1))
        .andExpect(jsonPath("$.studies[0].sites").isArray())
        .andExpect(jsonPath("$.studies[0].sites[0].id").value(siteEntity.getId()))
        .andExpect(jsonPath("$.message", is(MessageCode.GET_SITES_SUCCESS.getMessage())));
  }

  @Test
  public void shouldReturnNotFoundForGetSites() throws Exception {

    HttpHeaders headers = newCommonHeaders();
    // Step 1: set the userId to invalid
    headers.set(USER_ID_HEADER, IdGenerator.id());

    // Step 2: Call API to return SITE_NOT_FOUND errorDescription
    mockMvc
        .perform(
            get(ApiEndpoint.GET_SITES.getPath()).headers(headers).contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error_description", is(ErrorCode.SITE_NOT_FOUND.getDescription())));
  }

  @Test
  public void shouldReturnNotFoundForSiteParticipants() throws Exception {
    HttpHeaders headers = newCommonHeaders();

    // Call API to return SITE_NOT_FOUND errorDescription
    mockMvc
        .perform(
            get(ApiEndpoint.GET_SITE_PARTICIPANTS.getPath(), IdGenerator.id())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error_description", is(SITE_NOT_FOUND.getDescription())));
  }

  @Test
  public void shouldReturnForbiddenForGetSiteParticipants() throws Exception {
    HttpHeaders headers = newCommonHeaders();

    // set manage site permission to view only
    sitePermissionEntity = siteEntity.getSitePermissions().get(0);
    sitePermissionEntity.setCanEdit(Permission.READ_VIEW.value());
    testDataHelper.getSiteRepository().saveAndFlush(siteEntity);

    // Call API to return MANAGE_SITE_PERMISSION_ACCESS_DENIED errorDescription
    mockMvc
        .perform(
            get(ApiEndpoint.GET_SITE_PARTICIPANTS.getPath(), siteEntity.getId())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(
            jsonPath(
                "$.error_description", is(MANAGE_SITE_PERMISSION_ACCESS_DENIED.getDescription())));
  }

  @Test
  public void shouldReturnSiteParticipants() throws Exception {
    HttpHeaders headers = newCommonHeaders();

    siteEntity.setStudy(studyEntity);
    testDataHelper.getSiteRepository().saveAndFlush(siteEntity);
    participantRegistrySiteEntity.setOnboardingStatus(OnboardingStatus.NEW.getCode());
    testDataHelper
        .getParticipantRegistrySiteRepository()
        .saveAndFlush(participantRegistrySiteEntity);
    // Call API to return GET_PARTICIPANT_REGISTRY_SUCCESS errorDescription
    mockMvc
        .perform(
            get(ApiEndpoint.GET_SITE_PARTICIPANTS.getPath(), siteEntity.getId())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.participantRegistryDetail", notNullValue()))
        .andExpect(jsonPath("$.participantRegistryDetail.studyId", is(studyEntity.getId())))
        .andExpect(jsonPath("$.participantRegistryDetail.siteStatus", is(siteEntity.getStatus())))
        .andExpect(
            jsonPath("$.message", is(MessageCode.GET_PARTICIPANT_REGISTRY_SUCCESS.getMessage())));
  }

  @Test
  public void shouldReturnConflictForInvitingDisabledParticipant() throws Exception {
    appEntity.setOrgInfo(testDataHelper.createOrgInfo());
    studyEntity.setAppInfo(appEntity);
    siteEntity.setStudy(studyEntity);
    testDataHelper.getSiteRepository().save(siteEntity);
    HttpHeaders headers = newCommonHeaders();
    addAuthenticationHeaders(headers);

    // Step 1: Disabled participant invite
    participantRegistrySiteEntity.setOnboardingStatus(OnboardingStatus.DISABLED.getCode());
    participantRegistrySiteRepository.saveAndFlush(participantRegistrySiteEntity);

    InviteParticipantRequest inviteParticipantRequest = new InviteParticipantRequest();
    inviteParticipantRequest.setIds(Arrays.asList(participantRegistrySiteEntity.getId()));
    // Step 2: call the API and assert the error description
    mockMvc
        .perform(
            post(ApiEndpoint.INVITE_PARTICIPANT.getPath(), siteEntity.getId())
                .content(JsonUtils.asJsonString(inviteParticipantRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.failedInvitations").isArray())
        .andExpect(jsonPath("$.failedInvitations", hasSize(1)))
        .andExpect(
            jsonPath("$.error_description", is(ErrorCode.EMAIL_FAILED_TO_IMPORT.getDescription())));
  }

  @Test
  public void shouldReturnSiteNotFoundForInviteParticipant() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    addAuthenticationHeaders(headers);

    InviteParticipantRequest inviteParticipantRequest = new InviteParticipantRequest();
    inviteParticipantRequest.setIds(Arrays.asList(participantRegistrySiteEntity.getId()));
    mockMvc
        .perform(
            post(ApiEndpoint.INVITE_PARTICIPANT.getPath(), IdGenerator.id())
                .content(JsonUtils.asJsonString(inviteParticipantRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath(
                "$.error_description", is(ErrorCode.SITE_NOT_EXIST_OR_INACTIVE.getDescription())));
  }

  @Test
  public void shouldInviteParticipant() throws Exception {
    appEntity.setOrgInfo(testDataHelper.createOrgInfo());
    studyEntity.setAppInfo(appEntity);
    siteEntity.setStudy(studyEntity);
    testDataHelper.getSiteRepository().save(siteEntity);
    participantRegistrySiteEntity.setEmail(TestDataHelper.EMAIL_VALUE);
    testDataHelper.getSiteRepository().save(siteEntity);
    testDataHelper.getParticipantRegistrySiteRepository().save(participantRegistrySiteEntity);

    // Step 1: New participant invite
    participantRegistrySiteEntity.setOnboardingStatus(OnboardingStatus.NEW.getCode());
    participantRegistrySiteRepository.saveAndFlush(participantRegistrySiteEntity);

    HttpHeaders headers = newCommonHeaders();
    addAuthenticationHeaders(headers);

    InviteParticipantRequest inviteParticipantRequest = new InviteParticipantRequest();
    inviteParticipantRequest.setIds(Arrays.asList(participantRegistrySiteEntity.getId()));
    // Step 2: call the API and expect PARTICIPANTS_INVITED_SUCCESS message
    MvcResult result =
        mockMvc
            .perform(
                post(ApiEndpoint.INVITE_PARTICIPANT.getPath(), siteEntity.getId())
                    .content(JsonUtils.asJsonString(inviteParticipantRequest))
                    .headers(headers)
                    .contextPath(getContextPath()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.failedInvitations").isArray())
            .andExpect(jsonPath("$.failedInvitations", hasSize(0)))
            .andExpect(
                jsonPath("$.message", is(MessageCode.PARTICIPANTS_INVITED_SUCCESS.getMessage())))
            .andReturn();

    // TODO  Madhurya N , is this correct way??
    // Step 3: verify updated values
    InviteParticipantResponse inviteParticipantResponse =
        new ObjectMapper()
            .readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<InviteParticipantResponse>() {});
    List<ParticipantRegistrySiteEntity> participantRegistrySite =
        participantRegistrySiteRepository.findAllById(inviteParticipantResponse.getSuccessIds());

    assertNotNull(participantRegistrySite);
    assertEquals(
        OnboardingStatus.INVITED.getCode(), participantRegistrySite.get(0).getOnboardingStatus());

    // Step 4: delete participant registery
    participantRegistrySiteRepository.deleteById(inviteParticipantResponse.getSuccessIds().get(0));
  }

  @Test
  public void shouldReturnParticipantDetails() throws Exception {

    // Step 1: Set data needed to get Participant details
    participantRegistrySiteEntity.getStudy().setAppInfo(appEntity);
    participantStudyEntity.setParticipantId("1");
    testDataHelper.getParticipantStudyRepository().saveAndFlush(participantStudyEntity);
    participantRegistrySiteEntity.setOnboardingStatus(OnboardingStatus.NEW.getCode());
    testDataHelper
        .getParticipantRegistrySiteRepository()
        .saveAndFlush(participantRegistrySiteEntity);
    siteEntity.setLocation(locationEntity);
    testDataHelper.getSiteRepository().saveAndFlush(siteEntity);

    HttpHeaders headers = newCommonHeaders();

    // Step 2: Call API to return GET_PARTICIPANT_DETAILS_SUCCESS message
    mockMvc
        .perform(
            get(
                    ApiEndpoint.GET_PARTICIPANT_DETAILS.getPath(),
                    participantRegistrySiteEntity.getId())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.participantDetails", notNullValue()))
        .andExpect(
            jsonPath(
                "$.participantDetails.participantRegistrySiteid",
                is(participantRegistrySiteEntity.getId())))
        .andExpect(jsonPath("$.participantDetails.customLocationId", is("OpenStudy02")))
        .andExpect(
            jsonPath("$.message", is(MessageCode.GET_PARTICIPANT_DETAILS_SUCCESS.getMessage())));
  }

  @Test
  public void shouldReturnErrorParticipantDetails() throws Exception {

    HttpHeaders headers = newCommonHeaders();

    // Call API to return GET_PARTICIPANTS_ERROR error_description
    mockMvc
        .perform(
            get(ApiEndpoint.GET_PARTICIPANT_DETAILS.getPath(), IdGenerator.id())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error_description", is(ErrorCode.GET_PARTICIPANTS_ERROR.getDescription())));
  }

  @Test
  public void shouldReturnAccessDeniedForGetParticipantDetails() throws Exception {

    // Step 1: Set userId to invalid
    HttpHeaders headers = newCommonHeaders();
    headers.set(USER_ID_HEADER, IdGenerator.id());

    // Step 2: Call API to return MANAGE_SITE_PERMISSION_ACCESS_DENIED error_description
    mockMvc
        .perform(
            get(
                    ApiEndpoint.GET_PARTICIPANT_DETAILS.getPath(),
                    participantRegistrySiteEntity.getId())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(
            jsonPath(
                "$.error_description",
                is(ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED.getDescription())));
  }

  @AfterEach
  public void cleanUp() {
    if (StringUtils.isNotEmpty(siteId)) {
      siteRepository.deleteById(siteId);
      siteId = null;
    }
    testDataHelper.getStudyConsentRepository().deleteAll();
    testDataHelper.getParticipantStudyRepository().deleteAll();
    testDataHelper.getParticipantRegistrySiteRepository().deleteAll();
    testDataHelper.getSiteRepository().deleteAll();
    testDataHelper.getStudyRepository().deleteAll();
    testDataHelper.getAppRepository().deleteAll();
    testDataHelper.getUserRegAdminRepository().deleteAll();
    testDataHelper.getLocationRepository().deleteAll();
  }

  private HttpHeaders newCommonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(Constants.USER_ID_HEADER, userRegAdminEntity.getId());
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  private SiteRequest newSiteRequest() {
    SiteRequest siteRequest = new SiteRequest();
    siteRequest.setStudyId(studyEntity.getId());
    siteRequest.setLocationId(locationEntity.getId());
    return siteRequest;
  }

  private ParticipantRequest newParticipantRequest() {
    ParticipantRequest participantRequest = new ParticipantRequest();
    participantRequest.setEmail(TestDataHelper.EMAIL_VALUE);
    return participantRequest;
  }

  private void addAuthenticationHeaders(HttpHeaders headers) {
    headers.add("clientId", "clientId");
    headers.add("secretKey", "secretKey");
    headers.add("auth", "auth");
    headers.add("urAdminAuthId", "ur admin authId");
  }
}
