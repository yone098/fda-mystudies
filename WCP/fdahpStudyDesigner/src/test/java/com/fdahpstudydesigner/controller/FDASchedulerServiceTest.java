/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.fdahpstudydesigner.controller;

import com.fdahpstudydesigner.common.BaseMockIT;
import com.fdahpstudydesigner.scheduler.FDASchedulerService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FDASchedulerServiceTest extends BaseMockIT {

  @Autowired FDASchedulerService fdaSchedulerService;

  @Test
  public void shouldSendPushNotification() {
    fdaSchedulerService.sendPushNotification();
  }
}
