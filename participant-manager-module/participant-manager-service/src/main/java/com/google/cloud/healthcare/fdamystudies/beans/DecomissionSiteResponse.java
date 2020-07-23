package com.google.cloud.healthcare.fdamystudies.beans;

import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class DecomissionSiteResponse extends BaseResponse {

  private String siteId;

  private Integer status;

  public DecomissionSiteResponse(ErrorCode errorCode) {
    super(errorCode);
  }

  public DecomissionSiteResponse(String siteId, Integer status, MessageCode messageCode) {
    super(messageCode);
    this.status = status;
    this.siteId = siteId;
  }
}
