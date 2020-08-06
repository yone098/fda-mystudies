package com.google.cloud.healthcare.fdamystudies.common;

import com.google.cloud.healthcare.fdamystudies.beans.AuditLogEventRequest;
import com.google.cloud.healthcare.fdamystudies.service.AuditEventService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParticipantManagerAuditLogHelper {

  @Autowired AuditEventService auditService;

  @Autowired private CommonApplicationPropertyConfig commonPropConfig;

  /*public AuditLogEventResponse logEvent1(AuditLogEvent eventEnum, AuditLogEventRequest aleRequest) {
    return logEvent(eventEnum, aleRequest, null);
  }*/

  public void logEvent(
      AuditLogEvent eventEnum, AuditLogEventRequest aleRequest, Map<String, String> values) {
    String description = eventEnum.getDescription();
    if (values != null) {
      values.put("site", aleRequest.getSiteId());
      values.put("study", aleRequest.getStudyId());

      description = PlaceholderReplacer.replaceNamedPlaceholders(description, values);
    }
    aleRequest.setDescription(description);
  }
}
