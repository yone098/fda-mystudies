package com.google.cloud.healthcare.fdamystudies.beans;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Component
@Scope(value = "prototype")
public class UserSitePermissionRequest {

  private String siteId;

  private String locationId;

  private String customLocationId;

  private String locationName;

  private String locationDescription;

  private boolean selected;

  private boolean disabled;

  private int permission;
}
