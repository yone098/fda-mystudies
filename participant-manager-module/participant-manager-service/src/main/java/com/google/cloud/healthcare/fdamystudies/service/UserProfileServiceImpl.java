/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.cloud.healthcare.fdamystudies.beans.UserProfileRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserProfileResponse;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.mapper.UserProfileMapper;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;

@Service
public class UserProfileServiceImpl implements UserProfileService {

  private XLogger logger = XLoggerFactory.getXLogger(StudyServiceImpl.class.getName());

  @Autowired private UserRegAdminRepository userRegAdminRepository;

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

  /*@Override
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

    if (!StringUtils.equals(authRegistrationResponse.getCode(), "200")) {
      return new SetUpAccountResponse(ErrorCode.REGISTRATION_FAILED_IN_AUTH_SERVER);
    }
    UserRegAdminEntity userRegAdminUser = optUsers.get();
    userRegAdminUser.setUrAdminAuthId(authRegistrationResponse.getUserId());
    userRegAdminUser.setFirstName(setUpAccountRequest.getFirstName());
    userRegAdminUser.setLastName(setUpAccountRequest.getLastName());
    userRegAdminUser.setStatus(UserAccountStatus.ACTIVE.getStatus());
    userRegAdminRepository.saveAndFlush(userRegAdminUser);

    return new SetUpAccountResponse(
        authRegistrationResponse.getUserId(), MessageCode.SET_UP_ACCOUNT_SUCCESS);
  }*/

  /*private AuthRegistrationResponse registerUserInAuthServer(
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

    ResponseEntity<UserResponse> response =
        restTemplate.postForEntity(authRegisterUrl, requestEntity, UserResponse.class);

    UserResponse userResponse = response.getBody();
    AuthRegistrationResponse authRegistrationResponse = new AuthRegistrationResponse();
    if (response.getStatusCode().is2xxSuccessful()) {
      authRegistrationResponse.setUserId(userResponse.getUserId());
    } else {
      authRegistrationResponse.setCode(String.valueOf(response.getStatusCodeValue()));
      authRegistrationResponse.setMessage(userResponse.getErrorDescription());
    }
    return authRegistrationResponse;
  }*/
}
