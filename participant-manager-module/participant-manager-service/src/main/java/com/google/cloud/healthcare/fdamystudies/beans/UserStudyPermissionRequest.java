package com.google.cloud.healthcare.fdamystudies.beans;

import java.util.List;
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
public class UserStudyPermissionRequest {

  private String studyId;

  private String customStudyId;

  private String studyName;

  private boolean selected;

  private boolean disabled;

  private Integer permission;

  private List<UserSitePermissionRequest> sites;
}
