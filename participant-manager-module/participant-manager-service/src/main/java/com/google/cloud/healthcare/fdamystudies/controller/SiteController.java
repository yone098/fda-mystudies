/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.controller;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.USER_ID_HEADER;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.healthcare.fdamystudies.beans.DecomissionSiteResponse;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetailResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteDetails;
import com.google.cloud.healthcare.fdamystudies.beans.SiteRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SiteResponse;
import com.google.cloud.healthcare.fdamystudies.service.SiteService;

@RestController
public class SiteController {

  private static final String STATUS_LOG = "status=%d ";

  private static final String BEGIN_REQUEST_LOG = "%s request";

  private XLogger logger = XLoggerFactory.getXLogger(SiteController.class.getName());

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

  @PutMapping(
      value = "/sites/{siteId}/decommission",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DecomissionSiteResponse> decomissionSite(
      @RequestHeader(name = USER_ID_HEADER) String userId,
      @PathVariable String siteId,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());

    DecomissionSiteResponse decomissionSiteResponse = siteService.decomissionSite(userId, siteId);

    logger.exit(String.format(STATUS_LOG, decomissionSiteResponse.getHttpStatusCode()));
    return ResponseEntity.status(decomissionSiteResponse.getHttpStatusCode())
        .body(decomissionSiteResponse);
  }

  @PostMapping(
      value = "/sites/{siteId}/participants",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> addNewParticipant(
      @PathVariable String siteId,
      @RequestHeader(name = USER_ID_HEADER) String userId,
      @RequestBody ParticipantRequest participant,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());
    participant.setSiteId(siteId);
    ParticipantResponse participantResponse = siteService.addNewParticipant(participant, userId);
    logger.exit(String.format(STATUS_LOG, participantResponse.getHttpStatusCode()));
    return ResponseEntity.status(participantResponse.getHttpStatusCode()).body(participantResponse);
  }

  @PostMapping("/sites/{siteId}/participants/invite")
  public ResponseEntity<InviteParticipantResponse> inviteParticipants(
      @RequestBody InviteParticipantRequest inviteParticipantRequest,
      @PathVariable("siteId") String siteId,
      @RequestHeader(name = USER_ID_HEADER) String userId,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());

    inviteParticipantRequest.setSiteId(siteId);
    inviteParticipantRequest.setUserId(userId);

    InviteParticipantResponse inviteParticipantResponse =
        siteService.inviteParticipants(inviteParticipantRequest);

    logger.exit(String.format(STATUS_LOG, inviteParticipantResponse.getHttpStatusCode()));

    return ResponseEntity.status(inviteParticipantResponse.getHttpStatusCode())
        .body(inviteParticipantResponse);
  }

  @GetMapping("/sites")
  public ResponseEntity<SiteDetails> getSites(
      @RequestHeader(name = USER_ID_HEADER) String userId, HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());

    SiteDetails siteDetails = siteService.getSites(userId);

    logger.exit(String.format(STATUS_LOG, siteDetails.getHttpStatusCode()));
    return ResponseEntity.status(siteDetails.getHttpStatusCode()).body(siteDetails);
  }

  @GetMapping("/sites/{participantRegistrySite}/participant")
  public ResponseEntity<ParticipantDetailResponse> getParticipantDetails(
      @PathVariable("participantRegistrySite") String participantRegistrySiteId,
      @RequestHeader(name = USER_ID_HEADER) String userId,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());

    ParticipantDetailResponse participantDetails =
        siteService.getParticipantDetails(participantRegistrySiteId, userId);

    logger.exit(String.format(STATUS_LOG, participantDetails.getHttpStatusCode()));
    return ResponseEntity.status(participantDetails.getHttpStatusCode()).body(participantDetails);
  }

  @GetMapping(value = "/sites/{siteId}/participants", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ParticipantRegistryResponse> getSiteParticipant(
      @PathVariable String siteId,
      @RequestHeader(name = USER_ID_HEADER) String userId,
      @RequestParam(name = "onboardingStatus", defaultValue = "all") String onboardingStatus,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());

    ParticipantRegistryResponse participants =
        siteService.getParticipants(userId, siteId, onboardingStatus);
    logger.exit(String.format(STATUS_LOG, participants.getHttpStatusCode()));
    return ResponseEntity.status(participants.getHttpStatusCode()).body(participants);
  }
}
