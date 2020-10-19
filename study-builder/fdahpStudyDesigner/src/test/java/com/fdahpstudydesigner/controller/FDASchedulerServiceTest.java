/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.fdahpstudydesigner.controller;

import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.NOTIFICATION_METADATA_SEND_OPERATION_FAILED;

import com.fdahpstudydesigner.bean.PushNotificationBean;
import com.fdahpstudydesigner.common.BaseMockIT;
import com.fdahpstudydesigner.dao.NotificationDAO;
import com.fdahpstudydesigner.scheduler.FDASchedulerService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.ClientProtocolException;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

public class FDASchedulerServiceTest extends BaseMockIT {

  @Autowired FDASchedulerService fdaSchedulerService;

  @Autowired NotificationDAO notificationDao;

  @Ignore
  @Test
  public void shouldFailSendPushNotification() throws ClientProtocolException, IOException {

    PushNotificationBean bean = new PushNotificationBean();
    bean.setNotificationType("GT");
    List<PushNotificationBean> list = new ArrayList<>();
    list.add(bean);
    Mockito.when(notificationDao.getPushNotificationList(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(list);

    // Current implementation doesn't support dependency injection or mocking HttpClient so the
    // method is throwing ConnectException: Connection refused: connect
    fdaSchedulerService.sendPushNotification();
    verifyAuditEventCall(NOTIFICATION_METADATA_SEND_OPERATION_FAILED);
  }
}
