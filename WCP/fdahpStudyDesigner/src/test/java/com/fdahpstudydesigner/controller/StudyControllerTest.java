/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.fdahpstudydesigner.controller;

import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.LAST_PUBLISHED_VERSION_OF_STUDY_VIEWED;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.NEW_STUDY_CREATION_INITIATED;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_ACCESSED_IN_EDIT_MODE;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_CONSENT_SECTIONS_MARKED_COMPLETE;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_NEW_NOTIFICATION_CREATED;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_NOTIFICATIONS_SECTION_MARKED_COMPLETE;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_NOTIFICATION_MARKED_COMPLETE;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_NOTIFICATION_SAVED_OR_UPDATED;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_QUESTIONNAIRES_SECTION_MARKED_COMPLETE;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_RESOURCE_SECTION_MARKED_COMPLETE;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_VIEWED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fdahpstudydesigner.bean.StudySessionBean;
import com.fdahpstudydesigner.bo.NotificationBO;
import com.fdahpstudydesigner.common.BaseMockIT;
import com.fdahpstudydesigner.common.PathMappingUri;
import com.fdahpstudydesigner.util.FdahpStudyDesignerConstants;
import com.fdahpstudydesigner.util.SessionObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class StudyControllerTest extends BaseMockIT {

  private static final String STUDY_ID_VALUE = "678574";

  @Test
  public void shouldMarkActiveTaskAsCompleted() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    HashMap<String, Object> sessionAttributes = getSessionAttributes();
    sessionAttributes.put("0" + FdahpStudyDesignerConstants.STUDY_ID, STUDY_ID_VALUE);
    sessionAttributes.put("0" + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID, STUDY_ID_VALUE);

    mockMvc
        .perform(
            get(PathMappingUri.CONSENT_MARKED_AS_COMPLETE.getPath())
                .headers(headers)
                .sessionAttrs(sessionAttributes))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:comprehensionQuestionList.do"));

    verifyAuditEventCall(STUDY_CONSENT_SECTIONS_MARKED_COMPLETE);
  }

  @Test
  public void shouldMarkNotificationAsComplete() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    HashMap<String, Object> sessionAttributes = getSessionAttributes();
    sessionAttributes.put("0" + FdahpStudyDesignerConstants.STUDY_ID, STUDY_ID_VALUE);
    sessionAttributes.put("0" + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID, STUDY_ID_VALUE);

    mockMvc
        .perform(
            get(PathMappingUri.NOTIFICATION_MARK_AS_COMPLETED.getPath())
                .headers(headers)
                .sessionAttrs(sessionAttributes))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:getChecklist.do"));

    verifyAuditEventCall(STUDY_NOTIFICATIONS_SECTION_MARKED_COMPLETE);
  }

  @Test
  public void shouldMarkQuestionaireSectionAsComplete() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    HashMap<String, Object> sessionAttributes = getSessionAttributes();
    sessionAttributes.put("0" + FdahpStudyDesignerConstants.STUDY_ID, STUDY_ID_VALUE);
    sessionAttributes.put("0" + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID, STUDY_ID_VALUE);

    mockMvc
        .perform(
            get(PathMappingUri.QUESTIONAIRE_MARK_AS_COMPLETED.getPath())
                .headers(headers)
                .sessionAttrs(sessionAttributes))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:viewStudyActiveTasks.do"));

    verifyAuditEventCall(STUDY_QUESTIONNAIRES_SECTION_MARKED_COMPLETE);
  }

  @Test
  public void shouldMarkStudyResourceSectionAsComplete() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    HashMap<String, Object> sessionAttributes = getSessionAttributes();
    sessionAttributes.put("0" + FdahpStudyDesignerConstants.STUDY_ID, STUDY_ID_VALUE);
    sessionAttributes.put("0" + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID, STUDY_ID_VALUE);

    mockMvc
        .perform(
            get(PathMappingUri.RESOURCE_MARK_AS_COMPLETED.getPath())
                .headers(headers)
                .sessionAttrs(sessionAttributes))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:viewStudyNotificationList.do"));

    verifyAuditEventCall(STUDY_RESOURCE_SECTION_MARKED_COMPLETE);
  }

  @Test
  public void shouldSaveStudyInDraftState() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    HashMap<String, Object> sessionAttributes = getSessionAttributes();

    mockMvc
        .perform(
            get(PathMappingUri.SAVE_OR_UPDATE_BASIC_INFO.getPath())
                .headers(headers)
                .sessionAttrs(sessionAttributes))
        .andDo(print())
        .andExpect(status().isFound());

    // verifyAuditEventCall(STUDY_SAVED_IN_DRAFT_STATE);
  }

  @Test
  public void shouldSaveOrUpdateStudyResource() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.SAVE_OR_UPDATE_RESOURCE.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound());

    // verifyAuditEventCall(STUDY_RESOURCE_SAVED_OR_UPDATED);
  }

  @Test
  public void shouldMarkStudyResourceAsComplete() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.SAVE_OR_UPDATE_RESOURCE.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound());

    // verifyAuditEventCall(STUDY_RESOURCE_MARKED_COMPLETED);
  }

  @Test
  public void shouldSaveOrUpdateStudyEligibilitySection() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.SAVE_OR_UPDATE_STUDY_ELIGIBILITY.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound());

    // verifyAuditEventCall(STUDY_ELIGIBILITY_SECTION_SAVED_OR_UPDATED);
  }

  @Test
  public void shouldMarkStudyEligibilitySectionAsComplete() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.SAVE_OR_UPDATE_STUDY_ELIGIBILITY.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound());

    // verifyAuditEventCall(STUDY_ELIGIBILITY_SECTION_MARKED_COMPLETE);
  }

  @Test
  public void checkNewStudyCreationInitiated() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    StudySessionBean studySessionBean = new StudySessionBean();
    studySessionBean.setIsLive("");
    studySessionBean.setPermission("");
    studySessionBean.setStudyId("");
    studySessionBean.setSessionStudyCount(0);

    List<StudySessionBean> studySessionBeans = new ArrayList<>();
    studySessionBeans.add(studySessionBean);
    SessionObject session = new SessionObject();
    session.setStudySessionBeans(studySessionBeans);

    HashMap<String, Object> sessionAttributes = getSessionAttributes();
    sessionAttributes.put(FdahpStudyDesignerConstants.SESSION_OBJECT, session);

    mockMvc
        .perform(
            get(PathMappingUri.VIEW_STUDY_DETAILS.getPath())
                .headers(headers)
                .sessionAttrs(sessionAttributes))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/adminStudies/viewBasicInfo.do"));

    verifyAuditEventCall(NEW_STUDY_CREATION_INITIATED);
  }

  @Test
  public void checkLastPublishedVersionOfStudiedViewed() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    mockMvc
        .perform(
            get(PathMappingUri.VIEW_STUDY_DETAILS.getPath())
                .param(FdahpStudyDesignerConstants.STUDY_ID, STUDY_ID_VALUE)
                .param(FdahpStudyDesignerConstants.PERMISSION, "View")
                .param(FdahpStudyDesignerConstants.IS_LIVE, "isLive")
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/adminStudies/viewBasicInfo.do"));

    verifyAuditEventCall(LAST_PUBLISHED_VERSION_OF_STUDY_VIEWED);
  }

  @Test
  public void checkStudyViewed() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    mockMvc
        .perform(
            get(PathMappingUri.VIEW_STUDY_DETAILS.getPath())
                .param(FdahpStudyDesignerConstants.PERMISSION, "View")
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/adminStudies/viewBasicInfo.do"));

    verifyAuditEventCall(STUDY_VIEWED);
  }

  @Test
  public void checkStudyAccessedInEditMode() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    mockMvc
        .perform(
            get(PathMappingUri.VIEW_STUDY_DETAILS.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/adminStudies/viewBasicInfo.do"));

    verifyAuditEventCall(STUDY_ACCESSED_IN_EDIT_MODE);
  }

  @Test
  public void shouldUpdateStudyAction() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.UPDATE_STUDY_ACTION.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound());

    // verifyAuditEventCall(
    // STUDY_METADATA_SENT_TO_PARTICIPANT_DATASTORE,STUDY_METADATA_SEND_OPERATION_FAILED,
    // STUDY_METADATA_SENT_TO_RESPONSE_DATASTORE, STUDY_METADATA_SEND_FAILED);
  }

  @Test
  public void checkChecklistSavedOrCompleted() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.SAVE_OR_DONE_CHECKLIST.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound());

    // verifyAuditEventCall(auditEventMap,STUDY_CHECKLIST_SECTION_SAVED_OR_UPDATED,STUDY_CHECKLIST_SECTION_MARKED_COMPLETE);
  }

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

    verifyAuditEventCall(STUDY_NOTIFICATION_SAVED_OR_UPDATED);
  }

  @Test
  public void shouldSaveOrUpdateOrResendNotificationForAdd() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    NotificationBO notificationBo = new NotificationBO();
    notificationBo.setNotificationText("Study notification");

    MockHttpServletRequestBuilder requestBuilder =
        post(PathMappingUri.SAVE_OR_UPDATE_STUDY_NOTIFICATION.getPath())
            .headers(headers)
            .param("buttonType", "add")
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

  @Test
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
}
