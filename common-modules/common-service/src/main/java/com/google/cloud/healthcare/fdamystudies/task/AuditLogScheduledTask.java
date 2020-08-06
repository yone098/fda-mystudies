package com.google.cloud.healthcare.fdamystudies.task;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    value = "commonservice.auditlogevent.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class AuditLogScheduledTask {
  /*

    @Autowired private AuditEventService auditEventService;

    // 5min fixed delay and 10s initial delay
    @Scheduled(fixedDelay = 300000, initialDelay = 10000)
    public void executeSendAuditLogEventsTask() {
      auditEventService.resendAuditLogEvents();
    }
  */
}
