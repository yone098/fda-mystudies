/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.fdahpstudydesigner.controller;

import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_NEW_NOTIFICATION_CREATED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fdahpstudydesigner.bo.NotificationBO;
import com.fdahpstudydesigner.bo.ResourceBO;
import com.fdahpstudydesigner.common.BaseMockIT;
import com.fdahpstudydesigner.common.PathMappingUri;
import com.fdahpstudydesigner.util.FdahpStudyDesignerConstants;
import com.fdahpstudydesigner.util.SessionObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class StudyControllerTest extends BaseMockIT {

  private static final String STUDY_ID_VALUE = "678574";

  private static final String CUSTOM_STUDY_ID_VALUE = "678590";

  private static final String USER_ID_VALUE = "4878641";

  private static final int STUDY_ID_INT_VALUE = 678574;

  @Test
  public void shouldSaveOrUpdateOrResendNotificationForSave() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    NotificationBO notificationBo = new NotificationBO();
    notificationBo.setNotificationText("Study notification");

    MockHttpServletRequestBuilder requestBuilder =
        post(PathMappingUri.SAVE_OR_UPDATE_STUDY_NOTIFICATION.getPath())
            .headers(headers)
            .param("buttonType", "save")
            .sessionAttrs(getSessionAttributes());

    addParams(requestBuilder, notificationBo);

    mockMvc
        .perform(requestBuilder)
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:getStudyNotification.do"));

    verifyAuditEventCall(STUDY_NEW_NOTIFICATION_CREATED);
  }

  @Test
  public void shouldSaveOrUpdateOrResendNotificationForAdd() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    NotificationBO notificationBo = new NotificationBO();
    notificationBo.setNotificationText("Study notification");

    MockHttpServletRequestBuilder requestBuilder =
        post(PathMappingUri.SAVE_OR_UPDATE_STUDY_NOTIFICATION.getPath())
            .headers(headers)
            .param("buttonType", "resend")
            .sessionAttr("copyAppNotification", true)
            .sessionAttrs(getSessionAttributes());

    addParams(requestBuilder, notificationBo);

    mockMvc
        .perform(requestBuilder)
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/adminStudies/viewStudyNotificationList.do"));

    verifyAuditEventCall(STUDY_NEW_NOTIFICATION_CREATED);
  }

  /*@Test
    public void shouldSaveOrUpdateOrResendNotificationForDone() throws Exception {
      HttpHeaders headers = getCommonHeaders();

      NotificationBO notificationBo = new NotificationBO();
      notificationBo.setNotificationText("Study notification");

      MockHttpServletRequestBuilder requestBuilder =
          post(PathMappingUri.SAVE_OR_UPDATE_STUDY_NOTIFICATION.getPath())
              .headers(headers)
              .param("buttonType", "done")
              .sessionAttrs(getSessionAttributes());

      addParams(requestBuilder, notificationBo);

      mockMvc
          .perform(requestBuilder)
          .andDo(print())
          .andExpect(status().isFound())
          .andExpect(view().name("redirect:/adminStudies/viewStudyNotificationList.do"));

      verifyAuditEventCall(STUDY_NOTIFICATION_MARKED_COMPLETE);
    }
  */
  @Test
  public void shouldCreateNewStudyResource() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    SessionObject session = new SessionObject();
    session.setUserId(Integer.parseInt(USER_ID_VALUE));
    session.setStudySession(new ArrayList<>(Arrays.asList(0)));
    session.setSessionId(UUID.randomUUID().toString());

    HashMap<String, Object> sessionAttributes = getSessionAttributes();
    sessionAttributes.put(FdahpStudyDesignerConstants.SESSION_OBJECT, session);
    sessionAttributes.put(STUDY_ID_ATTR_NAME, STUDY_ID_VALUE);
    sessionAttributes.put(CUSTOM_STUDY_ID_ATTR_NAME, CUSTOM_STUDY_ID_VALUE);

    ResourceBO ResourceBO = new ResourceBO();
    ResourceBO.setAction(true);

    MockHttpServletRequestBuilder requestBuilder =
        post(PathMappingUri.SAVE_OR_UPDATE_RESOURCE.getPath())
            .headers(headers)
            .sessionAttrs(sessionAttributes);

    addParams(requestBuilder, ResourceBO);

    mockMvc.perform(requestBuilder).andDo(print()).andExpect(status().isFound());
  }
}
