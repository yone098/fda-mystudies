package com.fdahpstudydesigner.common;

public enum PathMappingUri {
  ACTIVATE_OR_DEACTIVATE_USER("/adminUsersEdit/activateOrDeactivateUser.do"),

  DELETE_QUESTION_FORM_INFO("/adminStudies/deleteFormQuestion.do"),

  SAVE_OR_UPDATE_QUETIONNAIR_SCHEDULE("/adminStudies/saveorUpdateQuestionnaireSchedule.do"),

  DELETE_QUESTIONNAIRES("/adminStudies/deleteQuestionnaire.do"),

  SESSION_OUT("/sessionOut.do"),

  CHANGE_PASSWORD("/adminDashboard/changePassword.do"),

  UPDATE_PROFILE_DETAILS("/adminDashboard/updateUserDetails.do"),

  VIEW_USER_DETAILS("/adminDashboard/viewUserDetails.do"),

  VIEW_NOTIFICATION_LIST("/adminNotificationView/viewNotificationList.do"),

  SAVE_OR_UPDATE_NOTIFICATION("/adminNotificationEdit/saveOrUpdateNotification.do"),

  SAVE_OR_UPDATE_STUDY_NOTIFICATION("/adminStudies/saveOrUpdateStudyNotification.do"),

  DELETE_QUESTIONNAIRE_STEP("/adminStudies/deleteQuestionnaireStep.do"),

  ACTIVE_TASK_MARK_AS_COMPLETED("/adminStudies/activeTAskMarkAsCompleted.do"),

  SAVE_OR_UPDATE_ACTIVE_TASK_CONTENT("/adminStudies/saveOrUpdateActiveTaskContent.do"),

  DELETE_ACTIVE_TASK("/adminStudies/deleteActiveTask.do"),

  CONSENT_MARKED_AS_COMPLETE("/adminStudies/consentMarkAsCompleted.do"),

  NOTIFICATION_MARK_AS_COMPLETED("/adminStudies/notificationMarkAsCompleted.do"),

  QUESTIONAIRE_MARK_AS_COMPLETED("/adminStudies/questionnaireMarkAsCompleted.do"),

  RESOURCE_MARK_AS_COMPLETED("/adminStudies/resourceMarkAsCompleted.do"),

  SAVE_OR_UPDATE_BASIC_INFO("/adminStudies/saveOrUpdateBasicInfo.do"),

  SAVE_OR_UPDATE_RESOURCE("/adminStudies/saveOrUpdateResource.do"),

  SAVE_OR_UPDATE_STUDY_ELIGIBILITY("/adminStudies/saveOrUpdateStudyEligibilty.do"),

  VIEW_STUDY_DETAILS("/adminStudies/viewStudyDetails.do"),

  UPDATE_STUDY_ACTION("/adminStudies/updateStudyAction"),

  SAVE_OR_DONE_CHECKLIST("/adminStudies/saveOrDoneChecklist.do");

  private final String path;

  private PathMappingUri(String path) {
    this.path = path;
  }

  public String getPath() {
    return this.path;
  }
}
