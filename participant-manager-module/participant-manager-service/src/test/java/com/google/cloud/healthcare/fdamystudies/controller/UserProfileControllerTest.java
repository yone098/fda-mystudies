package com.google.cloud.healthcare.fdamystudies.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.cloud.healthcare.fdamystudies.beans.AuditLogEventRequest;
import com.google.cloud.healthcare.fdamystudies.beans.PatchUserRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SetUpAccountRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserProfileRequest;
import com.google.cloud.healthcare.fdamystudies.common.ApiEndpoint;
import com.google.cloud.healthcare.fdamystudies.common.BaseMockIT;
import com.google.cloud.healthcare.fdamystudies.common.CommonConstants;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.IdGenerator;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent;
import com.google.cloud.healthcare.fdamystudies.common.UserStatus;
import com.google.cloud.healthcare.fdamystudies.helper.TestDataHelper;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;
import com.google.cloud.healthcare.fdamystudies.service.UserProfileService;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.google.cloud.healthcare.fdamystudies.common.JsonUtils.asJsonString;
import static com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent.USER_ACCOUNT_ACTIVATED;
import static com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent.USER_ACCOUNT_ACTIVATION_FAILED;
import static com.google.cloud.healthcare.fdamystudies.common.TestConstants.ADMIN_AUTH_ID_VALUE;
import static com.google.cloud.healthcare.fdamystudies.common.TestConstants.ADMIN_FIRST_NAME;
import static com.google.cloud.healthcare.fdamystudies.common.TestConstants.ADMIN_LAST_NAME;
import static com.google.cloud.healthcare.fdamystudies.common.TestConstants.USER_EMAIL_VALUE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserProfileControllerTest extends BaseMockIT {

  @Autowired private UserProfileController controller;

  @Autowired private UserProfileService userProfileService;

  @Autowired private TestDataHelper testDataHelper;

  private UserRegAdminEntity userRegAdminEntity;

  @Autowired UserRegAdminRepository userRegAdminRepository;

  protected MvcResult result;

  @Test
  @Disabled
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
  @Disabled
  public void shouldReturnUserProfile() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", VALID_BEARER_TOKEN);

    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_PROFILE.getPath(), TestDataHelper.ADMIN_AUTH_ID_VALUE)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", notNullValue()))
        .andExpect(jsonPath("$.firstName", is(TestDataHelper.ADMIN_FIRST_NAME)))
        .andExpect(jsonPath("$.lastName", is(TestDataHelper.ADMIN_LAST_NAME)))
        .andExpect(jsonPath("$.email", is(TestDataHelper.EMAIL_VALUE)))
        .andExpect(jsonPath("$.superAdmin", is(true)))
        .andExpect(jsonPath("$.message", is(MessageCode.GET_USER_PROFILE_SUCCESS.getMessage())));

    verifyTokenIntrospectRequest();
  }

  @Test
  @Disabled
  public void shouldReturnUserNotExistForUserProfile() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", VALID_BEARER_TOKEN);

    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_PROFILE.getPath(), IdGenerator.id())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error_description", is(ErrorCode.USER_NOT_EXISTS.getDescription())));

    verifyTokenIntrospectRequest();
  }

  @Test
  @Disabled
  public void shouldReturnUserNotActiveForUserProfile() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", VALID_BEARER_TOKEN);

    // Step 1: change the status to inactive
    userRegAdminEntity.setStatus(CommonConstants.INACTIVE_STATUS);
    userRegAdminRepository.saveAndFlush(userRegAdminEntity);

    // Step 2: Call API and expect error message USER_NOT_ACTIVE
    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_PROFILE.getPath(), TestDataHelper.ADMIN_AUTH_ID_VALUE)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error_description", is(ErrorCode.USER_NOT_ACTIVE.getDescription())));

    verifyTokenIntrospectRequest();
  }

  @Test
  @Disabled
  public void shouldReturnUserDetailsBySecurityCode() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", VALID_BEARER_TOKEN);

    mockMvc
        .perform(
            get(
                    ApiEndpoint.GET_USER_DETAILS_BY_SECURITY_CODE.getPath(),
                    userRegAdminEntity.getSecurityCode())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", notNullValue()))
        .andExpect(jsonPath("$.firstName", is(TestDataHelper.ADMIN_FIRST_NAME)))
        .andExpect(jsonPath("$.lastName", is(TestDataHelper.ADMIN_LAST_NAME)))
        .andExpect(jsonPath("$.email", is(TestDataHelper.EMAIL_VALUE)))
        .andExpect(
            jsonPath(
                "$.message",
                is(MessageCode.GET_USER_PROFILE_WITH_SECURITY_CODE_SUCCESS.getMessage())));
  }

  @Test
  @Disabled
  public void shouldReturnSecurityCodeExpired() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", VALID_BEARER_TOKEN);

    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_DETAILS_BY_SECURITY_CODE.getPath(), IdGenerator.id())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isGone())
        .andExpect(
            jsonPath("$.error_description", is(ErrorCode.SECURITY_CODE_EXPIRED.getDescription())));
  }

  @Test
  @Disabled
  public void shouldReturnUnauthorizedForUserDetailsBySecurityCode() throws Exception {

    // Step 1: change the security code expire date to before current date
    userRegAdminEntity.setSecurityCodeExpireDate(
        new Timestamp(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli()));
    userRegAdminRepository.saveAndFlush(userRegAdminEntity);

    // Step 2: Call API and expect error message SECURITY_CODE_EXPIRED
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", VALID_BEARER_TOKEN);
    headers.add("correlationId", IdGenerator.id());
    headers.add("appVersion", "1.0");
    headers.add("appId", "GCPMS001");
    headers.add("source", "PARTICIPANT MANAGER");
    mockMvc
        .perform(
            get(
                    ApiEndpoint.GET_USER_DETAILS_BY_SECURITY_CODE.getPath(),
                    userRegAdminEntity.getSecurityCode())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isGone())
        .andExpect(
            jsonPath("$.error_description", is(ErrorCode.SECURITY_CODE_EXPIRED.getDescription())));

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();

    Map<String, AuditLogEventRequest> auditEventMap = new HashedMap<>();
    auditEventMap.put(
        ParticipantManagerEvent.USER_ACCOUNT_ACTIVATION_FAILED_DUE_TO_EXPIRED_INVITATION
            .getEventCode(),
        auditRequest);

    verifyAuditEventCall(
        auditEventMap,
        ParticipantManagerEvent.USER_ACCOUNT_ACTIVATION_FAILED_DUE_TO_EXPIRED_INVITATION);
  }

  @Test
  @Disabled
  public void shouldUpdateUserProfile() throws Exception {
    // Step 1: Call API to update user profile
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER_PROFILE.getPath(), userRegAdminEntity.getId())
                .content(asJsonString(getUserProfileRequest()))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", is(MessageCode.PROFILE_UPDATE_SUCCESS.getMessage())));

    // Step 2: verify updated values
    Optional<UserRegAdminEntity> optUserRegAdminUser =
        userRegAdminRepository.findById(userRegAdminEntity.getId());
    UserRegAdminEntity userRegAdminEntity = optUserRegAdminUser.get();
    assertNotNull(userRegAdminEntity);
    assertEquals("mockito_updated", userRegAdminEntity.getFirstName());
    assertEquals("mockito_updated_last_name", userRegAdminEntity.getLastName());

    verifyTokenIntrospectRequest();
  }

  @Test
  @Disabled
  public void shouldReturnUserNotExistsForUpdatedUserDetails() throws Exception {
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER_PROFILE.getPath(), IdGenerator.id())
                .content(asJsonString(getUserProfileRequest()))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error_description", is(ErrorCode.USER_NOT_EXISTS.getDescription())));

    verifyTokenIntrospectRequest();
  }

  @Test
  @Disabled
  public void shouldReturnUserNotActiveForUpdatedUserDetails() throws Exception {
    // Step 1: change the status to inactive
    userRegAdminEntity.setStatus(CommonConstants.INACTIVE_STATUS);
    userRegAdminRepository.saveAndFlush(userRegAdminEntity);

    // Step 2: Call API and expect error USER_NOT_ACTIVE
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER_PROFILE.getPath(), userRegAdminEntity.getId())
                .content(asJsonString(getUserProfileRequest()))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error_description", is(ErrorCode.USER_NOT_ACTIVE.getDescription())));

    verifyTokenIntrospectRequest();
  }

  @Test
  @Disabled
  public void shouldSetUpNewAccount() throws Exception {
    // Step 1: Setting up the request for set up account
    SetUpAccountRequest request = setUpAccountRequest();
    userRegAdminEntity.setEmail(USER_EMAIL_VALUE);
    testDataHelper.getUserRegAdminRepository().saveAndFlush(userRegAdminEntity);

    // Step 2: Call the API and expect SET_UP_ACCOUNT_SUCCESS message
    HttpHeaders headers = testDataHelper.newCommonHeaders();

    mockMvc
        .perform(
            post(ApiEndpoint.SET_UP_ACCOUNT.getPath())
                .content(asJsonString(request))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value(MessageCode.SET_UP_ACCOUNT_SUCCESS.getMessage()))
        .andExpect(jsonPath("$.userId", notNullValue()))
        .andReturn();

    // Step 3: verify saved values
    Optional<UserRegAdminEntity> optUser = userRegAdminRepository.findByEmail(request.getEmail());
    UserRegAdminEntity user = optUser.get();
    assertEquals(request.getFirstName(), user.getFirstName());
    assertEquals(request.getLastName(), user.getLastName());

    verify(1, postRequestedFor(urlEqualTo("/oauth-scim-service/users")));

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    auditRequest.setUserId(user.getId());

    Map<String, AuditLogEventRequest> auditEventMap = new HashedMap<>();
    auditEventMap.put(USER_ACCOUNT_ACTIVATED.getEventCode(), auditRequest);

    verifyAuditEventCall(auditEventMap, USER_ACCOUNT_ACTIVATED);
  }

  @Test
  @Disabled
  public void shouldReturnUserNotInvitedError() throws Exception {
    // Step 1: Setting up the request
    SetUpAccountRequest request = setUpAccountRequest();
    request.setEmail("Invalid@grr.la");

    // Step 2: Call the API and expect USER_NOT_INVITED error
    HttpHeaders headers = testDataHelper.newCommonHeaders();

    mockMvc
        .perform(
            post(ApiEndpoint.SET_UP_ACCOUNT.getPath())
                .content(asJsonString(request))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error_description", is(ErrorCode.USER_NOT_INVITED.getDescription())));

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    auditRequest.setAppId("PARTICIPANT MANAGER");

    Map<String, AuditLogEventRequest> auditEventMap = new HashedMap<>();
    auditEventMap.put(USER_ACCOUNT_ACTIVATION_FAILED.getEventCode(), auditRequest);

    verifyAuditEventCall(auditEventMap, USER_ACCOUNT_ACTIVATION_FAILED);
  }

  @Test
  @Disabled
  public void shouldReturnAuthServerApplicationError() throws Exception {
    // Step 1: Setting up the request for AuthServerApplicationError
    SetUpAccountRequest request = setUpAccountRequest();
    userRegAdminEntity.setEmail(USER_EMAIL_VALUE);
    request.setPassword("AuthServerError@b0ston");
    testDataHelper.getUserRegAdminRepository().saveAndFlush(userRegAdminEntity);

    // Step 2: Call the API and expect APPLICATION_ERROR error
    HttpHeaders headers = testDataHelper.newCommonHeaders();

    mockMvc
        .perform(
            post(ApiEndpoint.SET_UP_ACCOUNT.getPath())
                .content(asJsonString(request))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(
            jsonPath("$.error_description", is(ErrorCode.APPLICATION_ERROR.getDescription())));
  }

  @Test
  @Disabled
  public void shouldReturnInternalServerError() throws Exception {
    // Step 1: Setting up the request for bad request
    SetUpAccountRequest request = setUpAccountRequest();
    userRegAdminEntity.setEmail(USER_EMAIL_VALUE);
    request.setPassword("AuthServerBadRequest@b0ston");
    testDataHelper.getUserRegAdminRepository().saveAndFlush(userRegAdminEntity);

    // Step 2: Call the API and expect BAD_REQUEST error
    HttpHeaders headers = testDataHelper.newCommonHeaders();

    mockMvc
        .perform(
            post(ApiEndpoint.SET_UP_ACCOUNT.getPath())
                .content(asJsonString(request))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isInternalServerError());
  }

  @Test
  @Disabled
  public void shouldDeactivateUserAccount() throws Exception {
    // Step 1: Setting up the request for deactivate account
    PatchUserRequest statusRequest = new PatchUserRequest();
    statusRequest.setStatus(UserStatus.DEACTIVATED.getValue());

    // Step 2: Call the API and expect DEACTIVATE_USER_SUCCESS message
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.add("userId", userRegAdminEntity.getId());

    mockMvc
        .perform(
            patch(ApiEndpoint.PATCH_USER.getPath(), userRegAdminEntity.getId())
                .content(asJsonString(statusRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(MessageCode.DEACTIVATE_USER_SUCCESS.getMessage()))
        .andReturn();

    // Step 3: verify updated values
    Optional<UserRegAdminEntity> optUser =
        userRegAdminRepository.findById(userRegAdminEntity.getId());
    UserRegAdminEntity user = optUser.get();
    assertEquals(UserStatus.DEACTIVATED.getValue(), user.getStatus());

    // verify external API call
    verify(
        1,
        putRequestedFor(
            urlEqualTo(String.format("/oauth-scim-service/users/%s", ADMIN_AUTH_ID_VALUE))));

    verifyTokenIntrospectRequest();
  }

  @Test
  @Disabled
  public void shouldReactivateUserAccount() throws Exception {
    // Step 1: Setting up the request for reactivate account
    PatchUserRequest statusRequest = new PatchUserRequest();
    statusRequest.setStatus(UserStatus.ACTIVE.getValue());

    // Step 2: Call the API and expect REACTIVATE_USER_SUCCESS message
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.add("userId", userRegAdminEntity.getId());

    mockMvc
        .perform(
            patch(ApiEndpoint.PATCH_USER.getPath(), userRegAdminEntity.getId())
                .content(asJsonString(statusRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(MessageCode.REACTIVATE_USER_SUCCESS.getMessage()))
        .andReturn();

    // Step 3: verify updated values
    Optional<UserRegAdminEntity> optUser =
        userRegAdminRepository.findById(userRegAdminEntity.getId());
    UserRegAdminEntity user = optUser.get();
    assertEquals(UserStatus.ACTIVE.getValue(), user.getStatus());

    // verify external API call
    verify(
        1,
        putRequestedFor(
            urlEqualTo(String.format("/oauth-scim-service/users/%s", ADMIN_AUTH_ID_VALUE))));

    verifyTokenIntrospectRequest();
  }

  @Test
  @Disabled
  public void shouldReturnUserNotFoundForDeactivateUser() throws Exception {
    // Step 2: Call the API and expect USER_NOT_FOUND error
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.add("userId", userRegAdminEntity.getId());
    PatchUserRequest statusRequest = new PatchUserRequest();
    statusRequest.setStatus(UserStatus.ACTIVE.getValue());

    mockMvc
        .perform(
            patch(ApiEndpoint.PATCH_USER.getPath(), IdGenerator.id())
                .content(asJsonString(statusRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error_description", is(ErrorCode.USER_NOT_FOUND.getDescription())));

    verifyTokenIntrospectRequest();
  }

  @Test
  @Disabled
  public void shouldReturnInvalidUserStatusError() throws Exception {
    // Call the API and expect USER_NOT_FOUND error
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    PatchUserRequest statusRequest = new PatchUserRequest();
    statusRequest.setStatus(null);

    mockMvc
        .perform(
            patch(ApiEndpoint.PATCH_USER.getPath(), IdGenerator.id())
                .content(asJsonString(statusRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest());

    verifyTokenIntrospectRequest();
  }

  @Test
  @Disabled
  public void shouldReturnBadRequestForDeactivateUser() throws Exception {
    // Step 1: set invalid urAdminAuthId
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    userRegAdminEntity.setUrAdminAuthId(IdGenerator.id());
    testDataHelper.getUserRegAdminRepository().saveAndFlush(userRegAdminEntity);

    // Step 2: Call the API and expect APPLICATION_ERROR error
    mockMvc
        .perform(
            patch(ApiEndpoint.PATCH_USER.getPath(), userRegAdminEntity.getId())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andDo(print())
        .andExpect(status().isBadRequest());

    verifyTokenIntrospectRequest();
  }

  @Test
  @Disabled
  public void shouldDeleteInvitedUser() throws Exception {
    // Step 1: set user status as invited
    userRegAdminEntity.setStatus(CommonConstants.INVITED_STATUS);
    testDataHelper.getUserRegAdminRepository().saveAndFlush(userRegAdminEntity);

    // Step 2: Call the API and expect INVITATION_DELETED_SUCCESSFULLY message
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.add("userId", userRegAdminEntity.getId());

    mockMvc
        .perform(
            delete(ApiEndpoint.DELETE_USER.getPath(), userRegAdminEntity.getId())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.message").value(MessageCode.INVITATION_DELETED_SUCCESSFULLY.getMessage()))
        .andReturn();
  }

  @Test
  @Disabled
  public void shouldNotDeleteActiveUserForDeleteInvitationRequest() throws Exception {
    // Call the API and expect CANNOT_DELETE_INVITATION error
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.add("userId", userRegAdminEntity.getId());

    mockMvc
        .perform(
            delete(ApiEndpoint.DELETE_USER.getPath(), userRegAdminEntity.getId())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(
            jsonPath(
                "$.error_description", is(ErrorCode.CANNOT_DELETE_INVITATION.getDescription())));
  }

  @Test
  @Disabled
  public void shouldReturnNotSuperAdminAccess() throws Exception {
    // Step 1: set user non super admin
    userRegAdminEntity.setSuperAdmin(false);
    testDataHelper.getUserRegAdminRepository().saveAndFlush(userRegAdminEntity);

    // Step 2: Call the API and expect NOT_SUPER_ADMIN_ACCESS error
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.add("userId", userRegAdminEntity.getId());

    mockMvc
        .perform(
            delete(ApiEndpoint.DELETE_USER.getPath(), userRegAdminEntity.getId())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(
            jsonPath("$.error_description", is(ErrorCode.NOT_SUPER_ADMIN_ACCESS.getDescription())));
  }

  @Test
  @Disabled
  public void shouldReturnUserNotFoundForDeleteInvitation() throws Exception {
    // Call the API and expect USER_NOT_FOUND error
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.add("userId", userRegAdminEntity.getId());

    mockMvc
        .perform(
            delete(ApiEndpoint.DELETE_USER.getPath(), IdGenerator.id())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error_description", is(ErrorCode.USER_NOT_FOUND.getDescription())));
  }

  @AfterEach
  public void clean() {
    testDataHelper.cleanUp();
  }

  private SetUpAccountRequest setUpAccountRequest() {
    SetUpAccountRequest request = new SetUpAccountRequest();
    request.setEmail(USER_EMAIL_VALUE);
    request.setFirstName(ADMIN_FIRST_NAME);
    request.setLastName(ADMIN_LAST_NAME);
    request.setPassword("Kantharaj#1123");
    return request;
  }

  public UserProfileRequest getUserProfileRequest() {
    UserProfileRequest userProfileRequest = new UserProfileRequest();
    userProfileRequest.setFirstName("mockito_updated");
    userProfileRequest.setLastName("mockito_updated_last_name");
    return userProfileRequest;
  }
}
