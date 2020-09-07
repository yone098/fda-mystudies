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
public class UserAppPermissionRequest {

  private String id;

  private String customId;

  private String name;

  private boolean selected;

  private Integer permission;

  private Integer invitedCount;

  private Integer enrolledCount;

  private Integer enrollmentPercentage;

  private Integer studiesCount;

  private Integer appUsersCount;

  private Integer totalSitesCount;

  private Integer selectedSitesCount;

  private List<UserStudyPermissionRequest> studies;
}
