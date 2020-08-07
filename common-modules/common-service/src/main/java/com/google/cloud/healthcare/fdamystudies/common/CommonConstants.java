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

  public static final Integer ACTIVE_STATUS = 1;

  public static final Integer PENDING_STATUS = 2;

  public static final Integer INACTIVE_STATUS = 0;

  public static final String SUCCESS = "SUCCESS";

  public static final String STUDY_ID_HEADER = "studyId";

  public static final String OPEN_STUDY = "OPEN";

  public static final String CLOSE_STUDY = "CLOSE";

  public static final String NOT_APPLICABLE = "NA";

  public static final String ACTIVE = "Active";

  public static final String INVITED = "Invited";

  public static final String DEACTIVATED = "Deactivated";

  public static final String OPEN = "Open";

  public static final String CLOSE = "Close";

  public static final String YET_TO_JOIN = "Yet to Join";

  public static final String STATUS_ACTIVE = "Active";

  public static final String STATUS_INACTIVE = "Inactive";

  public static final String STATUS_PENDING = "Pending";

  public static final String ENROLLED_STATUS = "Enrolled";

  public static final Integer READ_PERMISSION = 1;

  public static final Integer READ_AND_EDIT_PERMISSION = 2;

  public static final int VIEW_VALUE = 0;

  public static final String YET_TO_ENROLL = "Yet To Enroll";

  public static final String ONBOARDING_STATUS_ALL = "all";

  public static final SimpleDateFormat SDF_DATE_TIME = new SimpleDateFormat("MM/dd/yyyy");

  public static final int INVITED_STATUS = 2;

  public static final boolean SELECTED = true;

  public static final boolean UNSELECTED = false;

  public static final String STATUS_LOG = "status=%d";

  public static final String ERROR_CODE_LOG = "error code=%s";

  public static final String MESSAGE_CODE_LOG = "message code=%s";

  public static final String EMAIL_REGEX =
      "^[A-Za-z0-9_+]+([\\.-]?[A-Za-z0-9_+]+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$";

  public static final Double DEFAULT_PERCENTAGE = 100.00;
}
