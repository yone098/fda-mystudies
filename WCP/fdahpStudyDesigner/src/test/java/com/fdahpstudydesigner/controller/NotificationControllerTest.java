package com.fdahpstudydesigner.controller;

import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.APP_LEVEL_NOTIFICATION_CREATED;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.APP_LEVEL_NOTIFICATION_LIST_VIEWED;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.APP_LEVEL_NOTIFICATION_REPLICATED_FOR_RESEND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fdahpstudydesigner.bo.NotificationBO;
import com.fdahpstudydesigner.common.BaseMockIT;
import com.fdahpstudydesigner.common.PathMappingUri;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

public class NotificationControllerTest extends BaseMockIT {

  @Test
  public void shouldViewNotificationList() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.VIEW_NOTIFICATION_LIST.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(view().name("notificationListPage"));

    verifyAuditEventCall(APP_LEVEL_NOTIFICATION_LIST_VIEWED);
  }

  @Test
  public void shouldSaveOrUpdateOrResendNotification() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    NotificationBO notificationBo = new NotificationBO();
    notificationBo.setNotificationText("notificationText");
    mockMvc
        .perform(
            post(PathMappingUri.SAVE_OR_UPDATE_NOTIFICATION.getPath())
                .headers(headers)
                .content(asJsonString(notificationBo))
                .param("buttonType", "add")
                .sessionAttr("copyAppNotification", true)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/adminNotificationView/viewNotificationList.do"));

    verifyAuditEventCall(APP_LEVEL_NOTIFICATION_REPLICATED_FOR_RESEND);
  }

  @Test
  public void shouldSaveOrUpdateOrResendNotificationn() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    NotificationBO notificationBo = new NotificationBO();
    notificationBo.setNotificationText("notificationText");
    mockMvc
        .perform(
            post(PathMappingUri.SAVE_OR_UPDATE_NOTIFICATION.getPath())
                .headers(headers)
                .content(asJsonString(notificationBo))
                .param("buttonType", "add")
                .sessionAttr("copyAppNotification", false)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/adminNotificationView/viewNotificationList.do"));

    verifyAuditEventCall(APP_LEVEL_NOTIFICATION_CREATED);
  }
}
