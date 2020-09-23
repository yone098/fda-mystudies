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

public class StudyActiveTasksControllerTest extends BaseMockIT {

  @Test
  public void shouldMarkActiveTaskAsCompleted() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.ACTIVE_TASK_MARK_AS_COMPLETED.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("getResourceList.do"));

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_ACTIVE_TASK_SECTION_MARKED_COMPLETE.getEventCode(), auditRequest);
    // verifyAuditEventCall(auditEventMap, STUDY_ACTIVE_TASK_SECTION_MARKED_COMPLETE);
  }

  @Test
  public void shouldStudyActiveTaskMarkedComplete() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.SAVE_OR_UPDATE_ACTIVE_TASK_CONTENT.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("viewStudyActiveTasks.do"));

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_ACTIVE_TASK_MARKED_COMPLETE.getEventCode(), auditRequest);
    // verifyAuditEventCall(auditEventMap, STUDY_ACTIVE_TASK_MARKED_COMPLETE);
  }

  @Test
  public void shouldStudyActiveTaskSavedOrUpdate() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.SAVE_OR_UPDATE_ACTIVE_TASK_CONTENT.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("viewActiveTask.do"));

    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    Map<String, AuditLogEventRequest> auditEventMap = new HashMap<>();
    // auditEventMap.put(STUDY_ACTIVE_TASK_SAVED_OR_UPDATED.getEventCode(), auditRequest);
    // verifyAuditEventCall(auditEventMap, STUDY_ACTIVE_TASK_SAVED_OR_UPDATED);
  }
}
