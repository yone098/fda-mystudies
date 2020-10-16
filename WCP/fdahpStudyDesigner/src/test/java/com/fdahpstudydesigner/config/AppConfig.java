package com.fdahpstudydesigner.config;

import static org.mockito.Mockito.mock;

import com.fdahpstudydesigner.bean.PushNotificationBean;
import com.fdahpstudydesigner.dao.NotificationDAO;
import java.util.ArrayList;
import java.util.List;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockit")
@Configuration
public class AppConfig {
  @Bean
  @Primary
  public NotificationDAO emailNotification() throws Exception {
    NotificationDAO emailNotification = mock(NotificationDAO.class);
    PushNotificationBean bean = new PushNotificationBean();
    bean.setNotificationType("GT");
    List<PushNotificationBean> list = new ArrayList<>();
    list.add(bean);
    Mockito.when(
            emailNotification.getPushNotificationList(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(list);
    return emailNotification;
  }
}
