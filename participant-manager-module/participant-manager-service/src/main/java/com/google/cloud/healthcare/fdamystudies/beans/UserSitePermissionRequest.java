package com.google.cloud.healthcare.fdamystudies.beans;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

  private int permission;
}
