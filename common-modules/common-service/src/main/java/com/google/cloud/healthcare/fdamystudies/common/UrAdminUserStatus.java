/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.common;

public enum UrAdminUserStatus {
  ACTIVE(1),
  DEACTIVATED(0),
  INVITED(2);

  private int status = 1;

  private UrAdminUserStatus(int status) {
    this.status = status;
  }

  public int getStatus() {
    return status;
  }

  public static UrAdminUserStatus valueOf(int status) {
    for (UrAdminUserStatus type : UrAdminUserStatus.values()) {
      if (status == type.getStatus()) {
        return type;
      }
    }
    return null;
  }
}
