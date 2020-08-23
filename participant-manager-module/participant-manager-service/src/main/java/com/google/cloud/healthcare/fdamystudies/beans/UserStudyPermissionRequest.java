package com.google.cloud.healthcare.fdamystudies.beans;

import java.util.List;
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
public class UserStudyPermissionRequest {

  private String studyId;

  private String customStudyId;

  private String studyName;

  private boolean selected;

  private Integer permission;

  private Integer totalSitesCount;

  private Integer selectedSitesCount;

  private List<UserSitePermissionRequest> sites;
}
