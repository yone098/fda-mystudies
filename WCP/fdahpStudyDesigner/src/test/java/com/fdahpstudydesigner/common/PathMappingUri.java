package com.fdahpstudydesigner.common;

public enum PathMappingUri {
  ACTIVATE_OR_DEACTIVATE_USER("/adminUsersEdit/activateOrDeactivateUser.do"),

  SESSION_OUT("/sessionOut.do");

  private final String path;

  private PathMappingUri(String path) {
    this.path = path;
  }

  public String getPath() {
    return this.path;
  }
}
