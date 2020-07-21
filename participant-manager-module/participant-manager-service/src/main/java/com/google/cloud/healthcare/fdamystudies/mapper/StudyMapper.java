package com.google.cloud.healthcare.fdamystudies.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.google.cloud.healthcare.fdamystudies.beans.AppSiteResponse;
import com.google.cloud.healthcare.fdamystudies.beans.AppStudyResponse;
import com.google.cloud.healthcare.fdamystudies.beans.EnrolledStudies;
import com.google.cloud.healthcare.fdamystudies.beans.SiteDetails;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;

public final class StudyMapper {
  private StudyMapper() {}

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
      enrolledStudy.setCustomStudyId(entry.getKey().getCustomId());
      enrolledStudy.setStudyName(entry.getKey().getName());
      enrolledStudy.setStudyId(entry.getKey().getId());
      List<SiteDetails> sites = SiteMapper.toParticipantSiteList(entry);
      enrolledStudy.setSites(sites);
    }
    return enrolledStudy;
  }
}
