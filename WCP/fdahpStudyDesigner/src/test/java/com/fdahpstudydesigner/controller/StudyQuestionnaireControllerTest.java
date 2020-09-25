package com.fdahpstudydesigner.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fdahpstudydesigner.common.BaseMockIT;
import com.fdahpstudydesigner.common.PathMappingUri;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

public class StudyQuestionnaireControllerTest extends BaseMockIT {

  @Test
  public void shouldViewNotificationList() throws Exception {
    HttpHeaders headers = getCommonHeaders();
    mockMvc
        .perform(
            post(PathMappingUri.DELETE_QUESTIONNAIRE_STEP.getPath())
                .headers(headers)
                .param("stepId", "1")
                .param("questionnaireId", "1")
                .param("stepType", "Question")
                .sessionAttr("0customStudyId", "OpenStudy002")
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isOk());

    // verifyAuditEventCall(APP_LEVEL_NOTIFICATION_LIST_VIEWED);
  }
}
