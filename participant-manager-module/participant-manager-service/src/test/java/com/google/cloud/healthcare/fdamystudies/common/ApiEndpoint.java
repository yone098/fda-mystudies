/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.common;

import java.net.MalformedURLException;
import java.net.URL;

public enum ApiEndpoint {
  HEALTH("http://localhost:8080/participant-manager-service/v1/healthCheck"),

  ADD_NEW_SITE("http://localhost:8080/participant-manager-service/sites"),

  ADD_NEW_LOCATION("http://localhost:8080/participant-manager-service/locations"),

  GET_APPS("http://localhost:8080/participant-manager-service/apps"),

  GET_STUDIES("http://localhost:8080/participant-manager-service/studies"),

  UPDATE_LOCATION("http://localhost:8080/participant-manager-service/locations/{locationId}"),

  GET_LOCATIONS("http://localhost:8003/participant-manager-service/locations"),

  GET_LOCATION_BY_LOCATION_ID(
      "http://localhost:8003/participant-manager-service/locations/{locationId}"),

  GET_USER_PROFILE("http://localhost:8080/participant-manager-service/users/{userId}"),

  UPDATE_USER_PROFILE("http://localhost:8080/participant-manager-service/users/{userId}/profile"),

  GET_USER_DETAILS_BY_SECURITY_CODE(
      "http://localhost:8080/participant-manager-service/users/securitycodes/{securityCode}"),

  GET_STUDY_PARTICIPANT(
      "http://localhost:8080/participant-manager-service/studies/{studyId}/participants"),

  DECOMISSION_SITE("http://localhost:8080/participant-manager-service/sites/{siteId}/decommission"),

  ADD_NEW_PARTICIPANT(
      "http://localhost:8080/participant-manager-service/sites/{siteId}/participants"),

  GET_SITES("http://localhost:8080/participant-manager-service/sites"),

  GET_SITE_PARTICIPANTS(
      "http://localhost:8080/participant-manager-service/sites/{siteId}/participants"),

  INVITE_PARTICIPANTS(
      "http://localhost:8080/participant-manager-service/sites/{siteId}/participants/invite"),

  GET_APP_PARTICIPANTS(
      "http://localhost:8080/participant-manager-service/apps/{appId}/participants"),

  GET_PARTICIPANT_DETAILS(
      "http://localhost:8080/participant-manager-service/sites/{participantRegistrySite}/participant"),

  ADD_NEW_USER("http://localhost:8080/participant-manager-service/users"),

  UPDATE_USER("http://localhost:8080/participant-manager-service/users/{superAdminUserId}/"),

  IMPORT_PARTICIPANT(
      "http://localhost:8080/participant-manager-service/sites/{siteId}/participants/import"),

  UPDATE_ONBOARDING_STATUS(
      "http://localhost:8080/participant-manager-service/sites/{siteId}/participants/status"),

  GET_CONSENT_DOCUMENT(
      "http://localhost:8080/participant-manager-service/consents/{consentId}/consentDocument"),

  MANAGE_USERS("http://localhost:8080/participant-manager-service/users/"),

  MANAGE_ADMIN_DETAILS("http://localhost:8003/participant-manager-service/users/{adminId}"),

  UPDATE_TARGET_ENROLLMENT(
      "http://localhost:8080/participant-manager-service/studies/{studyId}/targetEnrollment"),

  SET_UP_ACCOUNT("http://localhost:8080/participant-manager-service/users/"),

  DEACTIVATE_ACCOUNT("http://localhost:8080/participant-manager-service/users/{userId}/deactivate");

  private String url;

  private ApiEndpoint(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public String getPath() throws MalformedURLException {
    return new URL(url).getPath();
  }
}
