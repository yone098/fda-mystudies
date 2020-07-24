/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.google.cloud.healthcare.fdamystudies.beans.AppSiteDetails;
import com.google.cloud.healthcare.fdamystudies.beans.AppSiteResponse;
import com.google.cloud.healthcare.fdamystudies.beans.AppStudyResponse;
import com.google.cloud.healthcare.fdamystudies.beans.EnrolledStudies;
import com.google.cloud.healthcare.fdamystudies.beans.StudyDetails;
import com.google.cloud.healthcare.fdamystudies.common.Permission;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
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
    studyDetail.setAppInfoId(study.getAppInfo().getId());

    if (studyPermissionsByStudyInfoId.get(study.getId()) != null) {
      Integer studyEditPermission = studyPermissionsByStudyInfoId.get(study.getId()).getEdit();
      // TODO (K)
      studyDetail.setStudyPermission(
          studyEditPermission == Permission.NO_PERMISSION.value()
              ? Permission.READ_VIEW.value()
              : Permission.READ_EDIT.value());
      studyDetail.setStudyPermission(studyEditPermission);
    }
    return studyDetail;
  }

  public static List<AppStudyResponse> toAppDetailsResponseList(
      List<StudyEntity> studies,
      Map<String, List<SiteEntity>> groupByStudyIdSiteMap,
      String[] fields) {
    List<AppStudyResponse> studyResponseList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(studies)) {
      for (StudyEntity study : studies) {
        AppStudyResponse appStudyResponse = new AppStudyResponse();
        appStudyResponse.setStudyId(study.getId());
        appStudyResponse.setCustomStudyId(study.getCustomId());
        appStudyResponse.setStudyName(study.getName());
        if (ArrayUtils.contains(fields, "sites")) {
          List<AppSiteResponse> appSiteResponsesList =
              SiteMapper.toAppDetailsResponseList(groupByStudyIdSiteMap.get(study.getId()));
          appStudyResponse.getSites().addAll(appSiteResponsesList);
        }
        studyResponseList.add(appStudyResponse);
      }
    }
    return studyResponseList;
  }

  public static EnrolledStudies toEnrolledStudies(
      Map<StudyEntity, List<ParticipantStudyEntity>> enrolledStudiesByStudyInfoId) {
    EnrolledStudies enrolledStudy = new EnrolledStudies();

    for (Entry<StudyEntity, List<ParticipantStudyEntity>> entry :
        enrolledStudiesByStudyInfoId.entrySet()) {
      StudyEntity study = entry.getKey();
      enrolledStudy.setCustomStudyId(study.getCustomId());
      enrolledStudy.setStudyName(study.getName());
      enrolledStudy.setStudyId(study.getId());
      List<AppSiteDetails> sites = SiteMapper.toParticipantSiteList(entry);
      enrolledStudy.setSites(sites);
    }
    return enrolledStudy;
  }
}
