package com.google.cloud.healthcare.fdamystudies.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.google.cloud.healthcare.fdamystudies.beans.UpdateUserProfileRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserProfileRequest;
import com.google.cloud.healthcare.fdamystudies.common.ApiEndpoint;
import com.google.cloud.healthcare.fdamystudies.common.BaseMockIT;
import com.google.cloud.healthcare.fdamystudies.common.CommonConstants;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.IdGenerator;
import com.google.cloud.healthcare.fdamystudies.common.JsonUtils;
import com.google.cloud.healthcare.fdamystudies.helper.TestDataHelper;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;
import com.google.cloud.healthcare.fdamystudies.service.UserProfileService;

public class UserProfileControllerTest extends BaseMockIT {

  @Autowired private UserProfileController controller;

  @Autowired private UserProfileService userProfileService;

  @Autowired private TestDataHelper testDataHelper;

  private UserRegAdminEntity userRegAdminEntity;

  @Autowired UserRegAdminRepository userRegAdminRepository;

  @Test
  public void contextLoads() {
    assertNotNull(controller);
    assertNotNull(mockMvc);
    assertNotNull(userProfileService);
  }

  @BeforeEach
  public void setUp() {
    userRegAdminEntity = testDataHelper.createUserRegAdmin();
  }

  @Test
  public void shouldReturnUserProfile() throws Exception {

    HttpHeaders headers = newCommonHeaders();
    headers.add("authUserId", testDataHelper.ADMIN_AUTH_ID_VALUE);

    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_PROFILE.getPath())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", notNullValue()))
        .andExpect(jsonPath("$.firstName", is("mockito")))
        .andExpect(jsonPath("$.lastName", is("mockito_last_name")))
        .andExpect(jsonPath("$.email", is("mockit_email@grr.la")))
        .andExpect(jsonPath("$.superAdmin", is(true)));
  }

  @Test
  public void shouldReturnUserNotExistForUserProfile() throws Exception {

    HttpHeaders headers = newCommonHeaders();
    headers.add("authUserId", IdGenerator.id());

    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_PROFILE.getPath())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error_description", is(ErrorCode.USER_NOT_EXISTS.getDescription())));
  }

  @Test
  public void shouldReturnUserNotActiveForUserProfile() throws Exception {

    userRegAdminEntity.setStatus(CommonConstants.INACTIVE_STATUS);
    userRegAdminRepository.saveAndFlush(userRegAdminEntity);

    HttpHeaders headers = newCommonHeaders();
    headers.add("authUserId", testDataHelper.ADMIN_AUTH_ID_VALUE);

    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_PROFILE.getPath())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error_description", is(ErrorCode.USER_NOT_ACTIVE.getDescription())));
  }

  @Test
  @Disabled("external api call failing")
  public void shouldUpdateUserProfile() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    // Step 1: Call API to create new location
    UpdateUserProfileRequest userInfo =
        new UpdateUserProfileRequest(
            "mockito_updated", "mockito_updated_last_name", "mockit_email_updated@grr.la");

    UserProfileRequest userProfileRequest =
        new UserProfileRequest(
            "mockit_email_updated@grr.la",
            "mockitoNewPassword@1234",
            "mockitoPassword@1234",
            "mockitoNewPassword@1234",
            "TuKUeFdyWz4E2A1-LqQcoYKBpMsfLnl-KjiuRFuxWcM3sQg",
            userInfo);

    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER_PROFILE.getPath())
                .content(JsonUtils.asJsonString(userProfileRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  public void shouldReturnUserDetails() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_DETAILS.getPath())
                .headers(headers)
                .param("securityCode", userRegAdminEntity.getSecurityCode())
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @AfterEach
  public void cleanUp() {
    testDataHelper.getUserRegAdminRepository().delete(userRegAdminEntity);
  }

  public HttpHeaders newCommonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
