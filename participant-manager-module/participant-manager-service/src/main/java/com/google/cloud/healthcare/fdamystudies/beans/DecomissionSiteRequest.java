package com.google.cloud.healthcare.fdamystudies.beans;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class DecomissionSiteRequest {

  @Size(max = 64)
  private String userId;

  @Size(max = 64)
  @NotBlank
  private String siteId;
}
