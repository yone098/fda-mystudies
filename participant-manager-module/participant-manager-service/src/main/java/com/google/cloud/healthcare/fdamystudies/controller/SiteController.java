/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.controller;

import static com.google.cloud.healthcare.fdamystudies.util.Constants.USER_ID_HEADER;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetail;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SiteResponse;
import com.google.cloud.healthcare.fdamystudies.service.SiteService;

@RestController
public class SiteController {

  private XLogger logger = XLoggerFactory.getXLogger(SiteController.class.getName());

  private static final String BEGIN_REQUEST_LOG = "%s request";

  private static final String STATUS_LOG = "status=%d ";

  @Autowired private SiteService siteService;

  @PostMapping(
      value = "/sites",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SiteResponse> addNewSite(
      @RequestHeader(name = USER_ID_HEADER) String userId,
      @Valid @RequestBody SiteRequest siteRequest,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());

    siteRequest.setUserId(userId);
    SiteResponse siteResponse = siteService.addSite(siteRequest);

    logger.exit(
        String.format(
            "status=%d and siteId=%s", siteResponse.getHttpStatusCode(), siteResponse.getSiteId()));

    return ResponseEntity.status(siteResponse.getHttpStatusCode()).body(siteResponse);
  }

  @PostMapping(
      value = "/sites/{siteId}/participants",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ParticipantResponse> addNewParticipant(
      @PathVariable String siteId,
      @RequestHeader(name = USER_ID_HEADER) String userId,
      @RequestBody ParticipantDetail participant,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());
    participant.setSiteId(siteId);
    ParticipantResponse participantResponse = siteService.addNewParticipant(participant, userId);
    logger.exit(String.format(STATUS_LOG, participantResponse.getHttpStatusCode()));
    return ResponseEntity.status(participantResponse.getHttpStatusCode()).body(participantResponse);
  }
}
