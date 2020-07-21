/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.controller;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.EDIT_VALUE;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.ENROLLED_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.VIEW_VALUE;
import static com.google.cloud.healthcare.fdamystudies.common.ErrorCode.EMAIL_EXISTS;
import static com.google.cloud.healthcare.fdamystudies.common.ErrorCode.ENROLLED_PARTICIPANT;
import static com.google.cloud.healthcare.fdamystudies.common.ErrorCode.MANAGE_SITE_PERMISSION_ACCESS_DENIED;
import static com.google.cloud.healthcare.fdamystudies.common.ErrorCode.OPEN_STUDY;
import static com.google.cloud.healthcare.fdamystudies.common.ErrorCode.SITE_NOT_EXIST_OR_INACTIVE;
import static com.google.cloud.healthcare.fdamystudies.common.JsonUtils.readJsonFile;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
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

import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SiteRequest;
import com.google.cloud.healthcare.fdamystudies.common.ApiEndpoint;
import com.google.cloud.healthcare.fdamystudies.common.BaseMockIT;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.IdGenerator;
import com.google.cloud.healthcare.fdamystudies.common.JsonUtils;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.helper.TestDataHelper;
import com.google.cloud.healthcare.fdamystudies.model.AppEntity;
import com.google.cloud.healthcare.fdamystudies.model.AppPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.LocationEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.SitePermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantRegistrySiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantStudyRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SiteRepository;
import com.google.cloud.healthcare.fdamystudies.service.SiteService;
import com.google.cloud.healthcare.fdamystudies.util.Constants;
import com.jayway.jsonpath.JsonPath;

public class SitesControllerTest extends BaseMockIT {

  private static String siteId;

  @Autowired private SiteController controller;
  @Autowired private SiteService siteService;
  @Autowired private TestDataHelper testDataHelper;
  @Autowired private SiteRepository siteRepository;
  @Autowired private ParticipantRegistrySiteRepository participantRegistrySiteRepository;
  @Autowired private ParticipantStudyRepository participantStudyRepository;

  private UserRegAdminEntity userRegAdminEntity;
  private StudyEntity studyEntity;
  private LocationEntity locationEntity;
  private AppEntity appEntity;
  private SiteEntity siteEntity;
  private SitePermissionEntity sitePermissionEntity;
  private ParticipantRegistrySiteEntity participantRegistrySiteEntity;
  private ParticipantStudyEntity participantStudyEntity;

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
    studyPermissionEntity.setEdit(VIEW_VALUE);
    studyEntity = testDataHelper.getStudyRepository().saveAndFlush(studyEntity);

    // pre-condition: deny app permission
    AppPermissionEntity appPermissionEntity = appEntity.getAppPermissions().get(0);
    appPermissionEntity.setEdit(VIEW_VALUE);
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
  public void shouldReturnBadRequestForAddNewParticipant() throws Exception {
    HttpHeaders headers = newCommonHeaders();

    // Call API to return SITE_NOT_EXIST_OR_INACTIVE errorDescription
    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_PARTICIPANT.getPath(), IdGenerator.id())
                .headers(headers)
                .content(JsonUtils.asJsonString(newParticipantRequest()))
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error_description").value(SITE_NOT_EXIST_OR_INACTIVE.getDescription()));

    // set participantStudy status to enrolled
    siteEntity.setStudy(studyEntity);
    participantStudyEntity.setStatus(ENROLLED_STATUS);
    participantRegistrySiteEntity.setEmail(newParticipantRequest().getEmail());
    participantStudyRepository.saveAndFlush(participantStudyEntity);

    // Call API to return ENROLLED_PARTICIPANT errorDescription
    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_PARTICIPANT.getPath(), siteEntity.getId())
                .headers(headers)
                .content(JsonUtils.asJsonString(newParticipantRequest()))
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error_description").value(ENROLLED_PARTICIPANT.getDescription()));
  }

  @Test
  public void shouldReturnEmailExistForAddNewParticipant() throws Exception {
    HttpHeaders headers = newCommonHeaders();

    // set emailId
    siteEntity.setStudy(studyEntity);
    participantRegistrySiteEntity.setEmail(newParticipantRequest().getEmail());
    participantRegistrySiteRepository.saveAndFlush(participantRegistrySiteEntity);

    // Call API to return EMAIL_EXISTS errorDescription
    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_PARTICIPANT.getPath(), siteEntity.getId())
                .headers(headers)
                .content(JsonUtils.asJsonString(newParticipantRequest()))
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error_description").value(EMAIL_EXISTS.getDescription()));
  }

  @Test
  public void shouldReturnForbiddenForAddNewParticipant() throws Exception {
    HttpHeaders headers = newCommonHeaders();

    // set manage site permission to view only
    sitePermissionEntity = siteEntity.getSitePermissions().get(0);
    sitePermissionEntity.setCanEdit(VIEW_VALUE);
    testDataHelper.getSiteRepository().saveAndFlush(siteEntity);

    // Call API to return MANAGE_SITE_PERMISSION_ACCESS_DENIED errorDescription
    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_PARTICIPANT.getPath(), siteEntity.getId())
                .headers(headers)
                .content(JsonUtils.asJsonString(newParticipantRequest()))
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(
            jsonPath("$.error_description")
                .value(MANAGE_SITE_PERMISSION_ACCESS_DENIED.getDescription()));

    // set study type to open study
    sitePermissionEntity.setCanEdit(EDIT_VALUE);
    studyEntity.setType("OPEN");
    siteEntity.setStudy(studyEntity);
    testDataHelper.getSiteRepository().saveAndFlush(siteEntity);
    // Call API to return OPEN_STUDY errorDescription
    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_PARTICIPANT.getPath(), siteEntity.getId())
                .headers(headers)
                .content(JsonUtils.asJsonString(newParticipantRequest()))
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error_description").value(OPEN_STUDY.getDescription()));
  }

  @Test
  public void shouldAddNewParticipant() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    siteEntity.setStudy(studyEntity);
    testDataHelper.getSiteRepository().saveAndFlush(siteEntity);
    ParticipantRequest participantRequest = newParticipantRequest();

    // Step 1: Call API to create new location
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

    // Step 2: verify saved values
    Optional<ParticipantRegistrySiteEntity> optParticipantRegistrySite =
        participantRegistrySiteRepository.findById(participantId);
    assertNotNull(optParticipantRegistrySite.get().getSite());
    assertEquals(siteEntity.getId(), optParticipantRegistrySite.get().getSite().getId());
    assertEquals(participantRequest.getEmail(), optParticipantRegistrySite.get().getEmail());
  }

  @AfterEach
  public void cleanUp() {
    if (StringUtils.isNotEmpty(siteId)) {
      siteRepository.deleteById(siteId);
      siteId = null;
    }
    testDataHelper.getParticipantStudyRepository().delete(participantStudyEntity);
    testDataHelper.getParticipantRegistrySiteRepository().delete(participantRegistrySiteEntity);
    testDataHelper.getSiteRepository().deleteAll();
    testDataHelper.getStudyRepository().delete(studyEntity);
    testDataHelper.getAppRepository().delete(appEntity);
    testDataHelper.getUserRegAdminRepository().delete(userRegAdminEntity);
    testDataHelper.getLocationRepository().delete(locationEntity);
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
}
