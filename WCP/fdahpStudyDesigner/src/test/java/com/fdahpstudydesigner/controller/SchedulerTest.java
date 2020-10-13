/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.fdahpstudydesigner.controller;

import com.fdahpstudydesigner.common.BaseMockIT;
import com.fdahpstudydesigner.config.ScheduledConfig;
import com.fdahpstudydesigner.scheduler.FDASchedulerService;
import org.awaitility.Duration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SchedulerTest extends BaseMockIT {

  @Test
  public void whenWaitOneSecond_thenScheduledIsCalledAtLeastTenTimes() {
    await()
        .atMost(Duration.ONE_MINUTE)
        .untilAsserted(() -> verify(counter, atLeast(10)).scheduled());
  }
}
