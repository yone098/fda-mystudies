/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.beans;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
@Getter
@Setter
public class ImportParticipantResponse extends BaseResponse {

  private List<ParticipantRequest> participants = new LinkedList<>();

  private Set<String> invalidEmails;

  private Set<String> duplicateEmails;

  public ImportParticipantResponse(ErrorCode errorCode) {
    super(errorCode);
  }

  public ImportParticipantResponse(MessageCode messageCode) {
    super(messageCode);
  }
}
