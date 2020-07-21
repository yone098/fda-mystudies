/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.mapper;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.SDF_DATE_TIME;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;

import com.google.cloud.healthcare.fdamystudies.beans.AppSiteResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteDetails;
import com.google.cloud.healthcare.fdamystudies.beans.SiteResponse;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;

public class SiteMapper {

  private SiteMapper() {}

  public static SiteResponse toSiteResponse(SiteEntity site) {
    SiteResponse response = new SiteResponse();
    response.setSiteId(site.getId());
    return response;
  }

  public static List<AppSiteResponse> toAppDetailsResponseList(List<SiteEntity> sites) {
    List<AppSiteResponse> siteResponseList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(sites)) {
      for (SiteEntity site : sites) {
        AppSiteResponse appSiteResponse = new AppSiteResponse();
        appSiteResponse.setSiteId(site.getId());
        appSiteResponse.setCustomLocationId(site.getLocation().getCustomId());
        appSiteResponse.setLocationDescription(site.getLocation().getDescription());
        appSiteResponse.setLocationId(site.getLocation().getId());
        appSiteResponse.setLocationName(site.getLocation().getName());
        siteResponseList.add(appSiteResponse);
      }
    }
    return siteResponseList;
  }

  public static List<SiteDetails> toParticipantSiteList(
      Entry<StudyEntity, List<ParticipantStudyEntity>> entry) {
    List<SiteDetails> sites = new ArrayList<>();
    for (ParticipantStudyEntity enrollment : entry.getValue()) {
      SiteDetails studiesEnrollment = new SiteDetails();
      studiesEnrollment.setCustomSiteId(enrollment.getSite().getLocation().getCustomId());
      studiesEnrollment.setSiteId(enrollment.getSite().getId());
      studiesEnrollment.setSiteName(enrollment.getSite().getLocation().getName());
      studiesEnrollment.setSiteStatus(enrollment.getStatus());
      studiesEnrollment.setWithdrawlDate(
          enrollment.getWithdrawalDate() != null
              ? enrollment
                  .getWithdrawalDate()
                  .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
              : "NA");
      studiesEnrollment.setEnrollmentDate(
          enrollment.getEnrolledDate() != null
              ? SDF_DATE_TIME.format(enrollment.getEnrolledDate())
              : "NA");
      sites.add(studiesEnrollment);
    }
    return sites;
  }
}
