package com.fdahpstudydesigner.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fdahpstudydesigner.bean.AuditLogEventRequest;
import com.fdahpstudydesigner.common.BaseMockIT;
import com.fdahpstudydesigner.common.PathMappingUri;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

public class StudyControllerTest extends BaseMockIT {

  @Test
  public void shouldMarkActiveTaskAsCompleted() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.CONSENT_MARKED_AS_COMPLETE.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:comprehensionQuestionList.do"));

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_CONSENT_SECTIONS_MARKED_COMPLETE.getEventCode(), auditRequest);
    // verifyAuditEventCall(auditEventMap, STUDY_CONSENT_SECTIONS_MARKED_COMPLETE);
  }

  @Test
  public void shouldMarkNotificationAsComplete() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.NOTIFICATION_MARK_AS_COMPLETED.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:getChecklist.do"));

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_NOTIFICATIONS_SECTION_MARKED_COMPLETE.getEventCode(),auditRequest);
    // verifyAuditEventCall(auditEventMap, STUDY_NOTIFICATIONS_SECTION_MARKED_COMPLETE);
  }

  @Test
  public void shouldMarkQuestionaireSectionAsComplete() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.QUESTIONAIRE_MARK_AS_COMPLETED.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:viewStudyActiveTasks.do"));

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_QUESTIONNAIRES_SECTION_MARKED_COMPLETE.getEventCode(),auditRequest);
    // verifyAuditEventCall(auditEventMap, STUDY_QUESTIONNAIRES_SECTION_MARKED_COMPLETE);
  }

  @Test
  public void shouldMarkStudyResourceSectionAsComplete() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.RESOURCE_MARK_AS_COMPLETED.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:viewStudyNotificationList.do"));

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_RESOURCE_SECTION_MARKED_COMPLETE.getEventCode(),auditRequest);
    // verifyAuditEventCall(auditEventMap, STUDY_RESOURCE_SECTION_MARKED_COMPLETE);
  }

  @Test
  public void shouldSaveStudyInDraftState() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.SAVE_OR_UPDATE_BASIC_INFO.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound());

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_SAVED_IN_DRAFT_STATE.getEventCode(),auditRequest);
    // verifyAuditEventCall(auditEventMap, STUDY_SAVED_IN_DRAFT_STATE);
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

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_RESOURCE_SAVED_OR_UPDATED.getEventCode(),auditRequest);
    // verifyAuditEventCall(auditEventMap, STUDY_RESOURCE_SAVED_OR_UPDATED);
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

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_RESOURCE_MARKED_COMPLETED.getEventCode(),auditRequest);
    // verifyAuditEventCall(auditEventMap, STUDY_RESOURCE_MARKED_COMPLETED);
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

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_ELIGIBILITY_SECTION_SAVED_OR_UPDATED.getEventCode(),auditRequest);
    // verifyAuditEventCall(auditEventMap, STUDY_ELIGIBILITY_SECTION_SAVED_OR_UPDATED);
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

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_ELIGIBILITY_SECTION_MARKED_COMPLETE.getEventCode(),auditRequest);
    // verifyAuditEventCall(auditEventMap, STUDY_ELIGIBILITY_SECTION_MARKED_COMPLETE);
  }

  @Test
  public void checkStudyDetails() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.VIEW_STUDY_DETAILS.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound());

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(NEW_STUDY_CREATION_INITIATED.getEventCode(),auditRequest);
    // auditEventMap.put(LAST_PUBLISHED_VERSION_OF_STUDY_VIEWED.getEventCode(),auditRequest);
    // auditEventMap.put(STUDY_VIEWED.getEventCode(),auditRequest);
    // auditEventMap.put(STUDY_ACCESSED_IN_EDIT_MODE.getEventCode(),auditRequest);
    //    verifyAuditEventCall(auditEventMap,
    // NEW_STUDY_CREATION_INITIATED,LAST_PUBLISHED_VERSION_OF_STUDY_VIEWED, STUDY_VIEWED,
    // STUDY_ACCESSED_IN_EDIT_MODE);
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

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_METADATA_SENT_TO_PARTICIPANT_DATASTORE.getEventCode(),auditRequest);
    // auditEventMap.put(STUDY_METADATA_SEND_OPERATION_FAILED.getEventCode(),auditRequest);
    // auditEventMap.put(STUDY_METADATA_SENT_TO_RESPONSE_DATASTORE.getEventCode(),auditRequest);
    // auditEventMap.put(STUDY_METADATA_SEND_FAILED.getEventCode(),auditRequest);
    //    verifyAuditEventCall(auditEventMap,
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

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_CHECKLIST_SECTION_SAVED_OR_UPDATED.getEventCode(),auditRequest);
    // auditEventMap.put(STUDY_CHECKLIST_SECTION_MARKED_COMPLETE.getEventCode(),auditRequest);
    //    verifyAuditEventCall(auditEventMap,
    // STUDY_CHECKLIST_SECTION_SAVED_OR_UPDATED,STUDY_CHECKLIST_SECTION_MARKED_COMPLETE);
  }
}
