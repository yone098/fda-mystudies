/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.fdahpstudydesigner.controller;

import com.fdahpstudydesigner.common.BaseMockIT;
import com.fdahpstudydesigner.dao.NotificationDAOImpl;
import com.fdahpstudydesigner.scheduler.FDASchedulerService;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

public class FDASchedulerServiceTest extends BaseMockIT {

  @InjectMocks @Autowired FDASchedulerService fdaSchedulerService;

  @Mock NotificationDAOImpl notify;

  // @Mock private HttpClient client;

  @Test
  public void shouldSendPushNotification() throws ClientProtocolException, IOException {
    //
    //    PushNotificationBean bean = new PushNotificationBean();
    //    bean.setNotificationType("GT");
    //    List<PushNotificationBean> list = new ArrayList<>();
    //    list.add(bean);
    //    NotificationDAO listMock = mock(NotificationDAO.class);
    //    when(listMock.getPushNotificationList(anyString(), anyString())).thenReturn(list);

    HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
    HttpResponse mockHttpResponse = Mockito.mock(HttpResponse.class);
    Mockito.when(mockHttpClient.execute(Mockito.isA(HttpPost.class))).thenReturn(mockHttpResponse);

    fdaSchedulerService.sendPushNotification();
  }
}
