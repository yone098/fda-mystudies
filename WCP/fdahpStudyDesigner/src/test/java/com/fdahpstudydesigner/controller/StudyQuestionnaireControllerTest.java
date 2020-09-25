package com.fdahpstudydesigner.controller;

import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_NEW_ACTIVE_TASK_CREATED;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_QUESTIONNAIRE_SAVED_OR_UPDATED;
import static com.fdahpstudydesigner.common.StudyBuilderAuditEvent.STUDY_QUESTION_STEP_IN_FORM_DELETED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fdahpstudydesigner.bo.QuestionnaireBo;
import com.fdahpstudydesigner.common.BaseMockIT;
import com.fdahpstudydesigner.common.JsonUtils;
import com.fdahpstudydesigner.common.PathMappingUri;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

public class StudyQuestionnaireControllerTest extends BaseMockIT {

  /* @Before
  public void setUp() {
    QuestionnaireBo questionnaireBo = new QuestionnaireBo();
  }*/

  @Test
  public void shouldDeleteStudyQuestionInForm() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    mockMvc
        .perform(
            post(PathMappingUri.DELETE_QUESTION_FORM_INFO.getPath())
                .headers(headers)
                .param("formId", "58")
                .param("questionId", "85199")
                .sessionAttr("0customStudyId", "OpenStudy003")
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isOk());

    verifyAuditEventCall(STUDY_QUESTION_STEP_IN_FORM_DELETED);
  }

  @Test
  public void shouldReturnStudyNewActiveTaskCreated() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    String str = 0 + "customStudyId";

    QuestionnaireBo questionnaireBo = new QuestionnaireBo();
    questionnaireBo.setId(null);
    mockMvc
        .perform(
            post(PathMappingUri.SAVE_OR_UPDATE_QUETIONNAIR_SCHEDULE.getPath())
                .headers(headers)
                .content(JsonUtils.asJsonString(questionnaireBo))
                .sessionAttr(str, "customStudyId")
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound());

    verifyAuditEventCall(STUDY_NEW_ACTIVE_TASK_CREATED);
  }

  @Test
  public void shouldSaveOrUpdateStudyActiveTask() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    String customStudyId = 0 + "customStudyId";
    String studyId = 0 + "studyId";

    mockMvc
        .perform(
            post(PathMappingUri.SAVE_OR_UPDATE_QUETIONNAIR_SCHEDULE.getPath())
                .headers(headers)
                // .content(JsonUtils.asJsonString(questionnaireBo))
                .sessionAttr(customStudyId, "customStudyId")
                .sessionAttr(studyId, "studyId")
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isOk());

    verifyAuditEventCall(STUDY_QUESTIONNAIRE_SAVED_OR_UPDATED);
  }

  @Test
  public void shouldDeleteQuestionnairies() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    mockMvc
        .perform(
            post(PathMappingUri.SHOULD_DELETE_QUESTIONNAIRES.getPath())
                .headers(headers)
                .param("questionnaireId", "85199")
                .param("studyId", "1")
                .sessionAttr("0customStudyId", "OpenStudy003")
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isOk());

    verifyAuditEventCall(STUDY_QUESTIONNAIRE_SAVED_OR_UPDATED);
  }
}
