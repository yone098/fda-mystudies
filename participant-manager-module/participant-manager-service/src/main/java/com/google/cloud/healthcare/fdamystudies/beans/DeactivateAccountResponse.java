package com.google.cloud.healthcare.fdamystudies.beans;

import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeactivateAccountResponse extends BaseResponse {

  private String tempRegId;

  public DeactivateAccountResponse(ErrorCode errorCode) {
    super(errorCode);
  }

  public DeactivateAccountResponse(String tempRegId, MessageCode messageCode) {
    super(messageCode);
    this.tempRegId = tempRegId;
  }
}
