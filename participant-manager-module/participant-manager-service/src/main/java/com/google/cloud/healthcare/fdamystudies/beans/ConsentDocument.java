package com.google.cloud.healthcare.fdamystudies.beans;

import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConsentDocument extends BaseResponse {
  private String version;
  private String type;
  private String content;

  public ConsentDocument(ErrorCode errorCode) {
    super(errorCode);
  }

  public ConsentDocument(MessageCode messageCode) {
    super(messageCode);
  }
}
