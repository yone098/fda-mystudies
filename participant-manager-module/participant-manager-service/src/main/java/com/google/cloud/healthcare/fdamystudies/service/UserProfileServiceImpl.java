/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.SUCCESS;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.google.cloud.healthcare.fdamystudies.beans.ChangePasswordRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ChangePasswordResponse;
import com.google.cloud.healthcare.fdamystudies.beans.UserProfileRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserProfileResponse;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.config.AppPropertyConfig;
import com.google.cloud.healthcare.fdamystudies.mapper.UserProfileMapper;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;

@Service
public class UserProfileServiceImpl implements UserProfileService {

  private XLogger logger = XLoggerFactory.getXLogger(StudyServiceImpl.class.getName());

  @Autowired private UserRegAdminRepository userRegAdminRepository;

  @Autowired private AppPropertyConfig appPropertyConfig;

  @Autowired private RestTemplate restTemplate;

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
    logger.entry("begin addNewLocation()");

    Optional<UserRegAdminEntity> optUserRegAdminUser =
        userRegAdminRepository.findByUrAdminAuthId(userProfileRequest.getUserId());

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

    String respMessage = changePassword(userProfileRequest);
    if (!respMessage.equalsIgnoreCase(SUCCESS)) {
      return new UserProfileResponse(ErrorCode.PROFILE_NOT_UPDATED);
    }
    UserProfileResponse profileResponse =
        new UserProfileResponse(MessageCode.PROFILE_UPDATED_SUCCESS);
    profileResponse.setUserId(adminUser.getId());
    logger.exit(String.format("message=%s", respMessage));
    return profileResponse;
  }

  // TODO Madhurya it has written in util class in old code
  public String changePassword(UserProfileRequest userProfileRequest) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    // TODO Madhurya appId and OrdId sent has 0 only in old code.....do i need to pass it as a
    // parameter in method??
    headers.set("appId", "0");
    headers.set("orgId", "0");
    headers.set("userId", userProfileRequest.getUserId());
    headers.set("clientId", appPropertyConfig.getClientId());
    headers.set("secretKey", appPropertyConfig.getSecretKey());

    ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
    changePasswordRequest.setCurrentPassword(userProfileRequest.getCurrentPswd());
    changePasswordRequest.setNewPassword(userProfileRequest.getNewPswd());
    HttpEntity<ChangePasswordRequest> requestBody =
        new HttpEntity<>(changePasswordRequest, headers);

    ResponseEntity<ChangePasswordResponse> responseEntity =
        restTemplate.exchange(
            appPropertyConfig.getAuthServerUrl()
                + "/users/"
                + userProfileRequest.getUserId()
                + "/change_password",
            HttpMethod.POST,
            requestBody,
            ChangePasswordResponse.class);

    if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
      return "";
    }
    ChangePasswordResponse responseBean = responseEntity.getBody();
    return responseBean == null ? "" : responseBean.getMessage();
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

  /*@Override
  @Transactional
  public SetUpAccountResponse saveUser(SetUpAccountRequest setUpAccountRequest) {

    Optional<UserRegAdminEntity> optUsers =
        userRegAdminRepository.findByEmail(setUpAccountRequest.getEmail());

    if (optUsers.isPresent()) {
      AuthRegistrationResponse authRegistrationResponse =
          registerUserInAuthServer(setUpAccountRequest);
    }

    return null;
  }

  private void validateSetUpAccountRequest(SetUpAccountRequest setUpAccountRequest) {
    logger.entry("validateSetUpAccountRequest()");

    Optional<UserRegAdminEntity> optUsers =
        userRegAdminRepository.findByEmail(setUpAccountRequest.getEmail());

    if (optUsers.isPresent()) {}
  }

  private AuthRegistrationResponse registerUserInAuthServer(
      SetUpAccountRequest setUpAccountRequest) {
    logger.entry("registerUserInAuthServer()");
    AuthRegistrationResponse authServerResponse = null;

    HttpHeaders headers = new HttpHeaders();
    headers.set("appId", "0");
    headers.set("orgId", "0");
    headers.set("clientId", appPropertyConfig.getClientId());
    headers.set("secretKey", appPropertyConfig.getSecretKey());

    AuthServerRegistrationBody authServerRegistrationRequest = new AuthServerRegistrationBody();
    authServerRegistrationRequest.setEmail(setUpAccountRequest.getEmail());
    authServerRegistrationRequest.setPassword(setUpAccountRequest.getPassword());

    HttpEntity<AuthServerRegistrationBody> request =
        new HttpEntity<>(authServerRegistrationRequest, headers);
    ObjectMapper objectMapper = null;
    RestTemplate template = new RestTemplate();
    ResponseEntity<?> responseEntity =
       // template.exchange(appPropertyConfig.getRegister(), HttpMethod.POST, request, String.class);
    return new AuthRegistrationResponse();
  }*/
}
