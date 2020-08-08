/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.beans.AuthRegistrationResponse;
import com.google.cloud.healthcare.fdamystudies.beans.AuthUserRequest;
import com.google.cloud.healthcare.fdamystudies.beans.DeactivateAccountResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SetUpAccountRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SetUpAccountResponse;
import com.google.cloud.healthcare.fdamystudies.beans.UpdateEmailStatusRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UpdateEmailStatusResponse;
import com.google.cloud.healthcare.fdamystudies.beans.UserProfileRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserProfileResponse;
import com.google.cloud.healthcare.fdamystudies.beans.UserResponse;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.common.UserAccountStatus;
import com.google.cloud.healthcare.fdamystudies.config.AppPropertyConfig;
import com.google.cloud.healthcare.fdamystudies.mapper.UserProfileMapper;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class UserProfileServiceImpl implements UserProfileService {

  private XLogger logger = XLoggerFactory.getXLogger(StudyServiceImpl.class.getName());

  @Autowired private UserRegAdminRepository userRegAdminRepository;

  @Autowired private AppPropertyConfig appPropertyConfig;

  @Autowired private RestTemplate restTemplate;

  @Autowired private OAuthService oauthService;

  @Override
  @Transactional(readOnly = true)
  public UserProfileResponse getUserProfile(String userId) {
    logger.entry("begin getUserProfile()");

    Optional<UserRegAdminEntity> optUserRegAdminUser =
        userRegAdminRepository.findByUrAdminAuthId(userId);

    if (!optUserRegAdminUser.isPresent()) {
      logger.exit(ErrorCode.USER_NOT_EXISTS);
      return new UserProfileResponse(ErrorCode.USER_NOT_EXISTS);
    }

    UserRegAdminEntity adminUser = optUserRegAdminUser.get();
    if (!adminUser.isActive()) {
      logger.exit(ErrorCode.USER_NOT_ACTIVE);
      return new UserProfileResponse(ErrorCode.USER_NOT_ACTIVE);
    }

    UserProfileResponse userProfileResponse =
        UserProfileMapper.toUserProfileResponse(adminUser, MessageCode.GET_USER_PROFILE_SUCCESS);
    logger.exit(userProfileResponse.getMessage());
    return userProfileResponse;
  }

  @Override
  @Transactional
  public UserProfileResponse updateUserProfile(UserProfileRequest userProfileRequest) {
    logger.entry("begin updateUserProfile()");

    Optional<UserRegAdminEntity> optUserRegAdminUser =
        userRegAdminRepository.findById(userProfileRequest.getUserId());

    if (!optUserRegAdminUser.isPresent()) {
      logger.exit(ErrorCode.USER_NOT_EXISTS);
      return new UserProfileResponse(ErrorCode.USER_NOT_EXISTS);
    }

    UserRegAdminEntity adminUser = optUserRegAdminUser.get();
    if (!adminUser.isActive()) {
      logger.exit(ErrorCode.USER_NOT_ACTIVE);
      return new UserProfileResponse(ErrorCode.USER_NOT_ACTIVE);
    }

    adminUser = UserProfileMapper.fromUserProfileRequest(userProfileRequest);
    adminUser = userRegAdminRepository.saveAndFlush(adminUser);

    UserProfileResponse profileResponse =
        new UserProfileResponse(MessageCode.PROFILE_UPDATE_SUCCESS);
    profileResponse.setUserId(adminUser.getId());
    logger.exit(MessageCode.PROFILE_UPDATE_SUCCESS);
    return profileResponse;
  }

  @Override
  @Transactional(readOnly = true)
  public UserProfileResponse findUserProfileBySecurityCode(String securityCode) {
    logger.entry("begin getUserProfileWithSecurityCode()");

    Optional<UserRegAdminEntity> optUserRegAdminUser =
        userRegAdminRepository.findBySecurityCode(securityCode);

    if (!optUserRegAdminUser.isPresent()) {
      logger.exit(ErrorCode.INVALID_SECURITY_CODE);
      return new UserProfileResponse(ErrorCode.INVALID_SECURITY_CODE);
    }

    UserRegAdminEntity user = optUserRegAdminUser.get();
    Timestamp now = new Timestamp(Instant.now().toEpochMilli());

    if (now.after(user.getSecurityCodeExpireDate())) {
      logger.exit(ErrorCode.SECURITY_CODE_EXPIRED);
      return new UserProfileResponse(ErrorCode.SECURITY_CODE_EXPIRED);
    }

    UserProfileResponse userProfileResponse =
        UserProfileMapper.toUserProfileResponse(
            user, MessageCode.GET_USER_PROFILE_WITH_SECURITY_CODE_SUCCESS);
    logger.exit(String.format("message=%s", userProfileResponse.getMessage()));
    return userProfileResponse;
  }

  @Override
  @Transactional
  public SetUpAccountResponse saveUser(SetUpAccountRequest setUpAccountRequest) {
    logger.entry("saveUser");

    Optional<UserRegAdminEntity> optUsers =
        userRegAdminRepository.findByEmail(setUpAccountRequest.getEmail());
    if (!optUsers.isPresent()) {
      return new SetUpAccountResponse(ErrorCode.USER_NOT_INVITED);
    }
    AuthRegistrationResponse authRegistrationResponse =
        registerUserInAuthServer(setUpAccountRequest);

    if (!StringUtils.equals(authRegistrationResponse.getCode(), "201")) {
      return new SetUpAccountResponse(ErrorCode.REGISTRATION_FAILED_IN_AUTH_SERVER);
    }
    UserRegAdminEntity userRegAdminUser = optUsers.get();
    userRegAdminUser.setUrAdminAuthId(authRegistrationResponse.getUserId());
    userRegAdminUser.setFirstName(setUpAccountRequest.getFirstName());
    userRegAdminUser.setLastName(setUpAccountRequest.getLastName());
    userRegAdminUser.setStatus(UserAccountStatus.ACTIVE.getStatus());
    userRegAdminRepository.saveAndFlush(userRegAdminUser);

    return new SetUpAccountResponse(
        authRegistrationResponse.getUserId(),
        authRegistrationResponse.getTempRegId(),
        MessageCode.SET_UP_ACCOUNT_SUCCESS);
  }

  private AuthRegistrationResponse registerUserInAuthServer(
      SetUpAccountRequest setUpAccountRequest) {
    logger.entry("registerUserInAuthServer()");
    AuthUserRequest userRequest = new AuthUserRequest();
    userRequest.setEmail(setUpAccountRequest.getEmail());
    userRequest.setPassword(setUpAccountRequest.getPassword());
    userRequest.setAppId("PARTICIPANT MANAGER");
    userRequest.setStatus(UserAccountStatus.PENDING_CONFIRMATION.getStatus());

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + oauthService.getAccessToken());

    HttpEntity<AuthUserRequest> requestEntity = new HttpEntity<>(userRequest, headers);

    String url = appPropertyConfig.getAuthRegisterUrl();
    ResponseEntity<UserResponse> response =
        restTemplate.postForEntity(url, requestEntity, UserResponse.class);

    UserResponse userResponse = response.getBody();
    AuthRegistrationResponse authRegistrationResponse = new AuthRegistrationResponse();
    if (response.getStatusCode().is2xxSuccessful()) {
      authRegistrationResponse.setUserId(userResponse.getUserId());
      authRegistrationResponse.setTempRegId(userResponse.getTempRegId());
      authRegistrationResponse.setCode(String.valueOf(response.getStatusCodeValue()));
    } else {
      authRegistrationResponse.setCode(String.valueOf(response.getStatusCodeValue()));
      authRegistrationResponse.setMessage(userResponse.getErrorDescription());
    }
    return authRegistrationResponse;
  }

  @Override
  public DeactivateAccountResponse deactivateAccount(String userId) {
    Optional<UserRegAdminEntity> optUsers = userRegAdminRepository.findById(userId);
    if (!optUsers.isPresent()) {
      return new DeactivateAccountResponse(ErrorCode.USER_NOT_FOUND);
    }
    UserRegAdminEntity user = optUsers.get();
    UpdateEmailStatusRequest updateEmailStatusRequest = new UpdateEmailStatusRequest();
    updateEmailStatusRequest.setStatus(UserAccountStatus.DEACTIVATED.getStatus());
    UpdateEmailStatusResponse response =
        updateUserInfoInAuthServer(updateEmailStatusRequest, user.getUrAdminAuthId());
    user.setStatus(UserAccountStatus.DEACTIVATED.getStatus());
    userRegAdminRepository.saveAndFlush(user);
    return new DeactivateAccountResponse(
        response.getTempRegId(), MessageCode.DEACTIVATE_USER_SUCCESS);
  }

  public UpdateEmailStatusResponse updateUserInfoInAuthServer(
      UpdateEmailStatusRequest updateEmailStatusRequest, String userId) {
    logger.entry("updateUserInfoInAuthServer()");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("Authorization", "Bearer " + oauthService.getAccessToken());

    HttpEntity<UpdateEmailStatusRequest> request =
        new HttpEntity<>(updateEmailStatusRequest, headers);

    ResponseEntity<UpdateEmailStatusResponse> responseEntity =
        restTemplate.exchange(
            appPropertyConfig.getAuthServerUpdateStatusUrl(),
            HttpMethod.PUT,
            request,
            UpdateEmailStatusResponse.class,
            userId);
    UpdateEmailStatusResponse updateEmailResponse = responseEntity.getBody();

    logger.debug(
        String.format(
            "status =%d, message=%s, error=%s",
            updateEmailResponse.getHttpStatusCode(),
            updateEmailResponse.getMessage(),
            updateEmailResponse.getErrorDescription()));
    return updateEmailResponse;
  }
}
