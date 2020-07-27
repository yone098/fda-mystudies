/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.beans;

import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthRegistrationResponse extends BaseResponse {

  private String title;
  private String appCode;

  private String userId;
  private String accessToken;
  private String clientToken;
  private String refreshToken;

  public AuthRegistrationResponse(ErrorCode errorCode) {
    super(errorCode);
  }
}
