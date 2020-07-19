/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import java.util.Optional;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
  public UserProfileResponse getUserProfile(String authUserId) {

    Optional<UserRegAdminEntity> optUserRegAdminUser =
        userRegAdminRepository.findByUrAdminAuthId(authUserId);
    // TODO Madhurya findByuseradminauthId so can we write in active user filter
    if (!optUserRegAdminUser.isPresent()) {
      logger.exit(
          String.format("Get user profile failed with error code=%s", ErrorCode.USER_NOT_EXISTS));
      return new UserProfileResponse(ErrorCode.USER_NOT_EXISTS);
    }
    UserRegAdminEntity adminUser = optUserRegAdminUser.get();
    if (!adminUser.isActive()) {
      logger.exit(
          String.format("Get user profile failed with error code=%s", ErrorCode.USER_NOT_ACTIVE));
      return new UserProfileResponse(ErrorCode.USER_NOT_ACTIVE);
    }

    return UserProfileMapper.toLocationResponse(
        adminUser, new UserProfileResponse(MessageCode.GET_USER_PROFILE_SUCCESS));
  }
}
