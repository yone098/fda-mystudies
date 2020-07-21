/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.mapper;

import java.util.Map;

import com.google.cloud.healthcare.fdamystudies.beans.StudyDetails;
import com.google.cloud.healthcare.fdamystudies.common.Permission;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyPermissionEntity;

public final class StudyMapper {

  private StudyMapper() {}

  public static StudyDetails setStudyDetails(
      Map<String, StudyPermissionEntity> studyPermissionsByStudyInfoId, StudyEntity study) {
    StudyDetails studyDetail = new StudyDetails();
    studyDetail.setId(study.getId());
    studyDetail.setCustomId(study.getCustomId());
    studyDetail.setName(study.getName());
    studyDetail.setType(study.getType());
    studyDetail.setAppId(study.getAppInfo().getAppId());
    studyDetail.setAppId(study.getAppInfo().getId());

    if (studyPermissionsByStudyInfoId.get(study.getId()) != null) {
      Integer studyEditPermission = studyPermissionsByStudyInfoId.get(study.getId()).getEdit();
      studyDetail.setStudyPermission(
          studyEditPermission == Permission.READ_VIEW.value()
              ? Permission.READ_VIEW.value()
              : Permission.READ_EDIT.value());
      studyDetail.setStudyPermission(studyEditPermission);
    }
    return studyDetail;
  }
}
