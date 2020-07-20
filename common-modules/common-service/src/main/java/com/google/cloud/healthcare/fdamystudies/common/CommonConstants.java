/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.common;

import java.text.SimpleDateFormat;

public final class CommonConstants {
  private CommonConstants() {}

  public static final String USER_ID_HEADER = "userId";

  public static final String YES = "Y";

  public static final String NO = "N";

  public static final int ACTIVE_STATUS = 1;

  public static final int INACTIVE_STATUS = 0;

  public static final String SUCCESS = "SUCCESS";

  public static final String STUDY_ID_HEADER = "studyId";

  public static final String OPEN_STUDY = "OPEN";

  public static final String INVITED_STATUS = "Invited";

  public static final String NEW_STATUS = "New";

  public static final String DISABLED_STATUS = "Disabled";

  public static final SimpleDateFormat SDF_DATE_TIME = new SimpleDateFormat("MM/dd/yyyy");
}
