package com.google.cloud.healthcare.fdamystudies.task;

import com.google.cloud.healthcare.fdamystudies.service.UserManagementProfileService;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeactivateAccountScheduledTask {

  private XLogger logger =
      XLoggerFactory.getXLogger(DeactivateAccountScheduledTask.class.getName());

  @Autowired UserManagementProfileService userManagementProfService;

  // 30min fixed delay and 10s initial delay
  @Scheduled(
      fixedDelayString = "${fixed.delay.milli.sec}",
      initialDelayString = "${initial.delay.milli.sec}")
  public void processDeactivatePendingRequests() {
    logger.entry("begin processDeactivatePendingRequests()");

    userManagementProfService.processDeactivatePendingRequests();

    logger.exit("processDeactivatePendingRequests() completed");
  }
}
