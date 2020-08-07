package com.google.cloud.healthcare.fdamystudies.common;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.cloud.healthcare.fdamystudies.beans.AuditLogEventRequest;
import com.google.cloud.healthcare.fdamystudies.service.AuditEventService;

@Component
public class ParticipantManagerAuditLogHelper {

  @Autowired AuditEventService auditService;

  public void logEvent(
      AuditLogEvent eventEnum, AuditLogEventRequest aleRequest, Map<String, String> values) {
    String description = eventEnum.getDescription();
    if (values != null) {
      description = PlaceholderReplacer.replaceNamedPlaceholders(description, values);
    }
    aleRequest.setDescription(description);
    auditService.postAuditLogEvent(aleRequest);
  }
}
