package com.fdahpstudydesigner.common;

public enum PathMappingUri {
  ACTIVATE_OR_DEACTIVATE_USER("/adminUsersEdit/activateOrDeactivateUser.do"),

  SESSION_OUT("/sessionOut.do"),

  CHANGE_PASSWORD("/adminDashboard/changePassword.do"),

  UPDATE_PROFILE_DETAILS("/adminDashboard/updateUserDetails.do"),

  VIEW_USER_DETAILS("/adminDashboard/viewUserDetails.do");

  private final String path;

  private PathMappingUri(String path) {
    this.path = path;
  }

  public String getPath() {
    return this.path;
  }
}
