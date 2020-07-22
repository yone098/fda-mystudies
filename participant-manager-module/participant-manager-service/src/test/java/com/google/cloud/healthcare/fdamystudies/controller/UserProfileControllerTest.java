package com.google.cloud.healthcare.fdamystudies.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.cloud.healthcare.fdamystudies.beans.UpdateUserProfileRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserProfileRequest;
import com.google.cloud.healthcare.fdamystudies.common.ApiEndpoint;
import com.google.cloud.healthcare.fdamystudies.common.BaseMockIT;
import com.google.cloud.healthcare.fdamystudies.common.CommonConstants;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.IdGenerator;
import com.google.cloud.healthcare.fdamystudies.common.JsonUtils;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.helper.TestDataHelper;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;
import com.google.cloud.healthcare.fdamystudies.service.UserProfileService;
import com.jayway.jsonpath.JsonPath;

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
    WireMock.resetAllRequests();
  }

  @Test
  public void shouldReturnUserProfile() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    headers.add("authUserId", TestDataHelper.ADMIN_AUTH_ID_VALUE);

    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_PROFILE.getPath())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", notNullValue()))
        .andExpect(jsonPath("$.firstName", is(TestDataHelper.FIRST_NAME)))
        .andExpect(jsonPath("$.lastName", is(TestDataHelper.LAST_NAME)))
        .andExpect(jsonPath("$.email", is(TestDataHelper.EMAIL_VALUE)))
        .andExpect(jsonPath("$.superAdmin", is(true)))
        .andExpect(jsonPath("$.message", is(MessageCode.GET_USER_PROFILE_SUCCESS.getMessage())));
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
    // Step 1: change the status to inactive
    userRegAdminEntity.setStatus(CommonConstants.INACTIVE_STATUS);
    userRegAdminRepository.saveAndFlush(userRegAdminEntity);

    // Step 2: Call API and expect error message USER_NOT_ACTIVE
    HttpHeaders headers = newCommonHeaders();
    headers.add("authUserId", TestDataHelper.ADMIN_AUTH_ID_VALUE);
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
  public void shouldUpdateUserProfile() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    UpdateUserProfileRequest userInfo =
        new UpdateUserProfileRequest(
            "mockito_updated", "mockito_updated_last_name", "mockit_email_updated@grr.la");

    UserProfileRequest userProfileRequest =
        new UserProfileRequest(
            "mockit_email_updated@grr.la",
            "mockitoNewPassword@1234",
            "mockitoPassword@1234",
            "mockitoNewPassword@1234",
            TestDataHelper.ADMIN_AUTH_ID_VALUE,
            userInfo);
    // Step 1: Call API to update user profile
    MvcResult result =
        mockMvc
            .perform(
                put(ApiEndpoint.UPDATE_USER_PROFILE.getPath())
                    .content(JsonUtils.asJsonString(userProfileRequest))
                    .headers(headers)
                    .contextPath(getContextPath()))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    String userId = JsonPath.read(result.getResponse().getContentAsString(), "$.userId");

    // Step 2: verify updated values
    Optional<UserRegAdminEntity> optUserRegAdminUser = userRegAdminRepository.findById(userId);
    UserRegAdminEntity userRegAdminEntity = optUserRegAdminUser.get();
    assertNotNull(userRegAdminEntity);
    assertEquals("mockit_email_updated@grr.la", userRegAdminEntity.getEmail());
    assertEquals("mockito_updated", userRegAdminEntity.getFirstName());
    assertEquals("mockito_updated_last_name", userRegAdminEntity.getLastName());
    // TODO........is this verify is correct??
    verify(
        1,
        postRequestedFor(
                urlEqualTo(
                    "/oauth-scim-service/users/TuKUeFdyWz4E2A1-LqQcoYKBpMsfLnl-KjiuRFuxWcM3sQg/change_password"))
            .withUrl(
                "/oauth-scim-service/users/TuKUeFdyWz4E2A1-LqQcoYKBpMsfLnl-KjiuRFuxWcM3sQg/change_password"));

    // Step 3: delete user profile
    userRegAdminRepository.deleteById(userId);
  }

  @Test
  public void shouldReturnUserNotExistsForUpdatedUserDetails() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    UserProfileRequest userProfileRequest =
        new UserProfileRequest(
            "mockit_email_updated@grr.la",
            "mockitoNewPassword@1234",
            "mockitoPassword@1234",
            "mockitoNewPassword@1234",
            IdGenerator.id(),
            new UpdateUserProfileRequest());

    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER_PROFILE.getPath())
                .content(JsonUtils.asJsonString(userProfileRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void shouldReturnUserNotActiveForUpdatedUserDetails() throws Exception {
    // Step 1: change the status to inactive
    userRegAdminEntity.setStatus(CommonConstants.INACTIVE_STATUS);
    userRegAdminRepository.saveAndFlush(userRegAdminEntity);

    HttpHeaders headers = newCommonHeaders();
    UserProfileRequest userProfileRequest =
        new UserProfileRequest(
            "mockit_email_updated@grr.la",
            "mockitoNewPassword@1234",
            "mockitoPassword@1234",
            "mockitoNewPassword@1234",
            TestDataHelper.ADMIN_AUTH_ID_VALUE,
            new UpdateUserProfileRequest());

    // Step 2: Call API
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER_PROFILE.getPath())
                .content(JsonUtils.asJsonString(userProfileRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldReturnUserDetailsWithSecurityCode() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_DETAILS.getPath())
                .headers(headers)
                .param("securityCode", userRegAdminEntity.getSecurityCode())
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", notNullValue()))
        .andExpect(jsonPath("$.firstName", is(TestDataHelper.FIRST_NAME)))
        .andExpect(jsonPath("$.lastName", is(TestDataHelper.LAST_NAME)))
        .andExpect(jsonPath("$.email", is(TestDataHelper.EMAIL_VALUE)))
        .andExpect(
            jsonPath(
                "$.message",
                is(MessageCode.GET_USER_PROFILE_WITH_SECURITY_CODE_SUCCESS.getMessage())));
  }

  @Test
  public void shouldReturnNotFoundForUserDetailsWithSecurityCode() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_DETAILS.getPath())
                .headers(headers)
                .param("securityCode", IdGenerator.id())
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.error_description", is(ErrorCode.INVALID_SECURITY_CODE.getDescription())));
  }

  @Test
  public void shouldReturnUnauthorizedForUserDetailsWithSecurityCode() throws Exception {
    // Step 1: change the security code expire date to before current date
    userRegAdminEntity.setSecurityCodeExpireDate(
        new Timestamp(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli()));
    userRegAdminRepository.saveAndFlush(userRegAdminEntity);
    HttpHeaders headers = newCommonHeaders();

    // Step 2: Call API and expect error message SECURITY_CODE_EXPIRED
    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_DETAILS.getPath())
                .headers(headers)
                .param("securityCode", userRegAdminEntity.getSecurityCode())
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(
            jsonPath("$.error_description", is(ErrorCode.SECURITY_CODE_EXPIRED.getDescription())));
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
