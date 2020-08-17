/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.beans;

import java.util.ArrayList;
import java.util.List;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class AdminDetailsResponse extends BaseResponse {

  private List<User> userList = new ArrayList<>();

  public AdminDetailsResponse(ErrorCode errorCode) {
    super(errorCode);
  }

  public AdminDetailsResponse(MessageCode messageCode, List<User> userList) {
    super(messageCode);
    this.userList.addAll(userList);
  }
}
