/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.model;

import java.sql.Timestamp;

public interface StudyInfo {

  String getCustomId();

  String getStudyId();

  String getStudyName();

  String getLogoImageUrl();

  Integer getEdit();

  String getType();

  Timestamp getCreatedTimestamp();

  boolean getStudyPermission();
}
