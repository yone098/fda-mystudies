/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.beans.AuditLogEventRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserAccountStatusResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SetUpAccountRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SetUpAccountResponse;
import com.google.cloud.healthcare.fdamystudies.beans.UserProfileRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserProfileResponse;
import com.google.cloud.healthcare.fdamystudies.beans.UserStatusRequest;

public interface UserProfileService {

  public UserProfileResponse getUserProfile(String userId);

  public UserProfileResponse updateUserProfile(
      UserProfileRequest userProfileRequest, AuditLogEventRequest auditRequest);

  public UserProfileResponse findUserProfileBySecurityCode(String securityCode);

  public UserAccountStatusResponse updateUserAccountStatus(
      UserStatusRequest statusRequest, AuditLogEventRequest auditRequest);

  public SetUpAccountResponse saveUser(
      SetUpAccountRequest setUpAccountRequest, AuditLogEventRequest auditRequest);
}
