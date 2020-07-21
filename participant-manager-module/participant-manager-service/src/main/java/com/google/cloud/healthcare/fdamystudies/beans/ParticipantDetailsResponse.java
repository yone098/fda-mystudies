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
public class ParticipantDetailsResponse extends BaseResponse {

  private String participantRegistrySiteid;

  private String siteId;

  private String customLocationId;

  private String locationName;

  private String customStudyId;

  private String studyName;

  private String customAppId;

  private String appName;

  private String onboardringStatus;

  private String email;

  private String invitationDate;

  private List<Enrollments> enrollments = new ArrayList<>();

  private List<ConsentHistory> consentHistory = new ArrayList<>();

  public ParticipantDetailsResponse(ErrorCode errorCode) {
    super(errorCode);
  }

  public ParticipantDetailsResponse(
      MessageCode messageCode, List<Enrollments> enrollments, List<ConsentHistory> consentHistory) {
    super(messageCode);
    this.enrollments.addAll(enrollments);
    this.consentHistory.addAll(consentHistory);
  }
}
