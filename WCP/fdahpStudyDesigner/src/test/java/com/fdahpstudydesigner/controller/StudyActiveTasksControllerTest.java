package com.fdahpstudydesigner.controller;

import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_ACTIVE_TASK_SECTION_MARKED_COMPLETE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fdahpstudydesigner.bo.ActiveTaskBo;
import com.fdahpstudydesigner.common.BaseMockIT;
import com.fdahpstudydesigner.common.PathMappingUri;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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
        .andExpect(view().name("redirect:/adminStudies/getResourceList.do"));

    verifyAuditEventCall(STUDY_ACTIVE_TASK_SECTION_MARKED_COMPLETE);
  }

  @Test
  public void shouldStudyActiveTaskMarkedComplete() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    ActiveTaskBo activeTaskBo = new ActiveTaskBo();
    activeTaskBo.setTaskTypeId(123);

    mockMvc
        .perform(
            get(PathMappingUri.SAVE_OR_UPDATE_ACTIVE_TASK_CONTENT.getPath())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(activeTaskBo))
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("viewStudyActiveTasks.do"));

    // verifyAuditEventCall(STUDY_ACTIVE_TASK_MARKED_COMPLETE);
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

    // verifyAuditEventCall(STUDY_ACTIVE_TASK_SAVED_OR_UPDATED);
  }
}
