package com.fdahpstudydesigner.common;

public enum PathMappingUri {
  ACTIVATE_OR_DEACTIVATE_USER("/adminUsersEdit/activateOrDeactivateUser.do"),

  DELETE_QUESTION_FORM_INFO("/adminStudies/deleteFormQuestion.do"),

  SAVE_OR_UPDATE_QUETIONNAIR_SCHEDULE("/adminStudies/saveorUpdateQuestionnaireSchedule.do"),

  SHOULD_DELETE_QUESTIONNAIRES("/adminStudies/deleteQuestionnaire.do"),

  SESSION_OUT("/sessionOut.do");

  private final String path;

  private PathMappingUri(String path) {
    this.path = path;
  }

  public String getPath() {
    return this.path;
  }
}
