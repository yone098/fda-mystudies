/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.beans;

import org.springframework.stereotype.Component;

import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Component
@Getter
@Setter
@ToString
public class SetUpAccountResponse extends BaseResponse {
  private String statusCode;
  private String message;
  private String accessToken;
  private String clientToken;
  private String userId;
  private String refreshToken;

  public SetUpAccountResponse(ErrorCode errorCode) {
    super(errorCode);
  }

  public SetUpAccountResponse(MessageCode messageCode) {
    super(messageCode);
  }
}
