package com.fdahpstudydesigner.common;

import com.fdahpstudydesigner.bean.AuditLogEventRequest;
import com.fdahpstudydesigner.mapper.AuditEventMapper;
import com.fdahpstudydesigner.service.AuditEventService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyBuilderAuditEventHelper {

  @Autowired AuditEventService auditService;

  //  @Autowired private CommonApplicationPropertyConfig commonPropConfig;

  public void logEvent(AuditLogEvent eventEnum, AuditLogEventRequest auditRequest) {
    logEvent(eventEnum, auditRequest, null);
  }

  public void logEvent(
      AuditLogEvent eventEnum, AuditLogEventRequest auditRequest, Map<String, String> values) {
    String description = eventEnum.getDescription();
    if (values != null) {
      description = PlaceholderReplacer.replaceNamedPlaceholders(description, values);
    }
    auditRequest.setDescription(description);

    auditRequest =
        /*AuditEventMapper.fromAuditLogEventEnumAndCommonPropConfig(
        eventEnum, commonPropConfig, auditRequest);*/
        AuditEventMapper.fromAuditLogEventEnumAndCommonPropConfig(eventEnum, auditRequest);
    auditService.postAuditLogEvent(auditRequest);
  }
}
