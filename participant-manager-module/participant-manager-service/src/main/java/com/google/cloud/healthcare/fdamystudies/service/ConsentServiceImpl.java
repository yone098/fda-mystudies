/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.beans.AuditLogEventRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ConsentDocument;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerAuditLogHelper;
import com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent;
import com.google.cloud.healthcare.fdamystudies.config.AppPropertyConfig;
import com.google.cloud.healthcare.fdamystudies.model.SitePermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyConsentEntity;
import com.google.cloud.healthcare.fdamystudies.repository.SitePermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyConsentRepository;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsentServiceImpl implements ConsentService {

  private XLogger logger = XLoggerFactory.getXLogger(ConsentServiceImpl.class.getName());

  @Autowired private SitePermissionRepository sitePermissionRepository;

  @Autowired private StudyConsentRepository studyConsentRepository;

  @Autowired private Storage storageService;

  @Autowired AppPropertyConfig appConfig;

  @Autowired private ParticipantManagerAuditLogHelper participantManagerHelper;

  @Override
  @Transactional(readOnly = true)
  public ConsentDocument getConsentDocument(
      String consentId, String userId, AuditLogEventRequest aleRequest) {
    logger.entry("begin getConsentDocument(consentId,userId)");

    Optional<StudyConsentEntity> optStudyConsent = studyConsentRepository.findById(consentId);
    StudyConsentEntity studyConsentEntity = optStudyConsent.get();

    if (!optStudyConsent.isPresent()
        || studyConsentEntity.getParticipantStudy() == null
        || studyConsentEntity.getParticipantStudy().getSite() == null) {
      logger.exit(ErrorCode.CONSENT_DATA_NOT_AVAILABLE);
      return new ConsentDocument(ErrorCode.CONSENT_DATA_NOT_AVAILABLE);
    }
    Optional<SitePermissionEntity> optSitePermission =
        sitePermissionRepository.findByUserIdAndSiteId(
            userId, studyConsentEntity.getParticipantStudy().getSite().getId());

    if (!optSitePermission.isPresent()) {
      logger.exit(ErrorCode.SITE_PERMISSION_ACCESS_DENIED);
      return new ConsentDocument(ErrorCode.SITE_PERMISSION_ACCESS_DENIED);
    }

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Blob blob =
            storageService.get(
                BlobId.of(appConfig.getBucketName(), studyConsentEntity.getPdfPath()));
    if (StringUtils.isNotBlank(studyConsentEntity.getPdfPath())) {
      try {
        blob.downloadTo(outputStream);
      } catch (StorageException e) {
        throw e;
      }
    }
    String document = new String(Base64.getEncoder().encode(blob.getContent()));

    Map<String, String> map =
        Stream.of(
                new String[][] {
                  {"site_id", studyConsentEntity.getParticipantStudy().getSite().getId()},
                  {"participant_id", studyConsentEntity.getParticipantStudy().getId()},
                  {"consent_version", studyConsentEntity.getVersion()}
                })
            .collect(Collectors.toMap(data -> data[0], data -> data[1]));
    participantManagerHelper.logEvent(
        ParticipantManagerEvent.CONSENT_DOCUMENT_DOWNLOADED, aleRequest, map);

    return new ConsentDocument(
        MessageCode.GET_CONSENT_DOCUMENT_SUCCESS,
        studyConsentEntity.getVersion(),
        MediaType.APPLICATION_PDF_VALUE,
        document);
  }
}
