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
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParticipantResponse extends BaseResponse {

  private String participantId;

  private List<AppParticipantRegistryResponse> appParticipantRegistryResponse = new ArrayList<>();

  public ParticipantResponse(ErrorCode errorCode) {
    super(errorCode);
  }

  public ParticipantResponse(MessageCode messageCode, String participantId) {
    super(messageCode);
    this.participantId = participantId;
  }

  public ParticipantResponse(
      MessageCode messageCode,
      List<AppParticipantRegistryResponse> appParticipantRegistryResponse) {
    super(messageCode);
    this.appParticipantRegistryResponse.addAll(appParticipantRegistryResponse);
  }
}
