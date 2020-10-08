/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import static com.google.cloud.healthcare.fdamystudies.common.JsonUtils.getObjectMapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.cloud.healthcare.fdamystudies.beans.AuditLogEventRequest;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
    value = "commonservice.auditlogevent.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class AuditEventServiceImpl implements AuditEventService {

  private XLogger logger = XLoggerFactory.getXLogger(AuditEventServiceImpl.class.getName());

  @Override
  public void postAuditLogEvent(AuditLogEventRequest auditRequest) {
    logger.entry(
        String.format("begin postAuditLogEvent() for %s event", auditRequest.getEventCode()));

    // The data to write to the log
    logger.debug("audit request=" + getObjectMapper().convertValue(auditRequest, JsonNode.class));

    logger.exit(
        String.format("postAuditLogEvent() for %s event finished", auditRequest.getEventCode()));
  }
}
