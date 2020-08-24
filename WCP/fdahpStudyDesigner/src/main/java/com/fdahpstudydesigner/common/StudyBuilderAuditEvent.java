/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.fdahpstudydesigner.common;

import java.util.Optional;

import static com.fdahpstudydesigner.common.PlatformComponent.PARTICIPANT_DATASTORE;
import static com.fdahpstudydesigner.common.PlatformComponent.RESPONSE_DATASTORE;
import static com.fdahpstudydesigner.common.PlatformComponent.STUDY_BUILDER;
import static com.fdahpstudydesigner.common.PlatformComponent.STUDY_DATASTORE;

public enum StudyBuilderAuditEvent implements AuditLogEvent {
  SIGNIN_SUCCEEDED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "SIGNIN_SUCCEEDED"),

  SIGNIN_FAILED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "SIGNIN_FAILED"),

  SIGNIN_FAILED_UNREGISTERED_USER(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Sign-in failure due to unregistered username.",
      "SIGNIN_FAILED_UNREGISTERED_USER"),

  PASSWORD_HELP_REQUESTED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "PASSWORD_HELP_REQUESTED"),

  USER_SIGNOUT_FAILED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "USER_SIGNOUT_FAILED"),

  PASSWORD_RESET_FAILED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "PASSWORD_RESET_FAILED"),

  ACCOUNT_LOCKED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "User account locked for ${lock_time} due to ${failed_attempt} consecutively failed sign-in attempts with incorrect password.",
      "ACCOUNT_LOCKED"),

  USER_SIGNOUT_SUCCEEDED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "USER_SIGNOUT_SUCCEEDED"),

  PASSWORD_CHANGE_ENFORCED_FOR_ALL_USERS(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Password change enforced for all users by signed-in user.",
      "PASSWORD_CHANGE_ENFORCED_FOR_ALL_USERS"),

  SESSION_EXPIRY(
      STUDY_BUILDER, STUDY_DATASTORE, null, "User session timed out or expired.", "SESSION_EXPIRY"),

  NEW_USER_CREATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "New user created (user ID - {new_user_id}, access level - {new_user_access_level}).",
      "NEW_USER_CREATED"),

  NEW_USER_INVITATION_EMAIL_SENT(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Account setup invitation email sent to user (user ID -{new_user_id}).",
      "NEW_USER_INVITATION_EMAIL_SENT"),

  NEW_USER_INVITATION_RESENT(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Account setup invitation email re-sent to user (user ID - {new_user_id}).",
      "NEW_USER_INVITATION_RESENT"),

  NEW_USER_ACCOUNT_ACTIVATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "NEW_USER_ACCOUNT_ACTIVATED"),

  NEW_USER_CREATION_FAILED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "NEW_USER_CREATION_FAILED"),

  NEW_USER_ACCOUNT_ACTIVATION_FAILED_INVALID_ACCESS_CODE(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Account activation failed for new user due to invalid access code (user ID - ${new_user_id}).",
      "NEW_USER_ACCOUNT_ACTIVATION_FAILED_INVALID_ACCESS_CODE"),

  NEW_USER_ACCOUNT_ACTIVATION_FAILED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Account activation failed for new user (user ID - {new_user_id}, access level - {new_user_access_level}).",
      "NEW_USER_ACCOUNT_ACTIVATION_FAILED"),

  USER_ACCOUNT_UPDATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Account details updated in My Account section.",
      "USER_ACCOUNT_UPDATED"),

  USER_ACCOUNT_UPDATED_FAILED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Attempt to update account details failed for user in My Account section.",
      "USER_ACCOUNT_UPDATED_FAILED"),

  USER_RECORD_UPDATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "User record updated (user id - {edited_user_id}, access level - {edited_user_access_level}).",
      "USER_RECORD_UPDATED"),

  USER_RECORD_DEACTIVATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "User account deactivated (user ID - ${edited_user_id}).",
      "USER_RECORD_DEACTIVATED"),

  USER_ACCOUNT_RE_ACTIVATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "User account activated (user ID - ${edited_user_id}).",
      "USER_ACCOUNT_RE-ACTIVATED"),

  PASSWORD_CHANGE_ENFORCED_FOR_USER(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Password change enforced for user (user id - {edited_user_id}).",
      "PASSWORD_CHANGE_ENFORCED_FOR_USER"),

  PASSWORD_HELP_EMAIL_SENT(
      STUDY_DATASTORE, STUDY_DATASTORE, null, null, "PASSWORD_HELP_EMAIL_SENT"),

  PASSWORD_RESET_SUCCEEDED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "PASSWORD_RESET_SUCCEEDED"),

  PASSWORD_RESET_EMAIL_SENT_FOR_LOCKED_ACCOUNT(
      STUDY_DATASTORE, STUDY_DATASTORE, null, null, "PASSWORD_RESET_EMAIL_SENT_FOR_LOCKED_ACCOUNT"),

  PASSWORD_CHANGE_SUCCEEDED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "PASSWORD_CHANGE_SUCCEEDED"),

  PASSWORD_CHANGE_FAILED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "PASSWORD_CHANGE_FAILED"),

  PASSWORD_HELP_EMAIL_FAILED(
      STUDY_DATASTORE, STUDY_DATASTORE, null, null, "PASSWORD_HELP_EMAIL_FAILED"),

  PASSWORD_RESET_EMAIL_FAILED_FOR_LOCKED_ACCOUNT(
      STUDY_DATASTORE,
      STUDY_DATASTORE,
      null,
      null,
      "PASSWORD_RESET_EMAIL_FAILED_FOR_LOCKED_ACCOUNT"),

  ACCOUNT_DETAILS_VIEWED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Account details viewed in My Account section.",
      "ACCOUNT_DETAILS_VIEWED"),

  USER_RECORD_VIEWED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "User record for user ID '${viewed_user_id}' viewed.",
      "USER_RECORD_VIEWED"),

  PASSWORD_ENFORCEMENT_EMAIL_SENT(
      STUDY_DATASTORE,
      STUDY_DATASTORE,
      null,
      "Password change enforcement email sent to user ID- '${edited_user_id}'.",
      "PASSWORD_ENFORCEMENT_EMAIL_SENT"),

  NEW_USER_INVITATION_EMAIL_FAILED(
      STUDY_DATASTORE,
      STUDY_DATASTORE,
      null,
      "Invitation email failed to be sent to new user (user ID - {new_user_id}).",
      "NEW_USER_INVITATION_EMAIL_FAILED"),

  PASSWORD_CHANGE_ENFORCEMENT_EMAIL_FAILED(
      STUDY_DATASTORE,
      STUDY_DATASTORE,
      null,
      "Password change enforcement email failed to be sent to User ID '${edited_user_id}'.",
      "PASSWORD_CHANGE_ENFORCEMENT_EMAIL_FAILED"),

  PASSWORD_CHANGE_ENFORCEMENT_FOR_ALL_USERS_EMAIL_SENT(
      STUDY_DATASTORE,
      STUDY_DATASTORE,
      null,
      "Password change enforcement email sent to all users barring superadmin(s).",
      "PASSWORD_CHANGE_ENFORCEMENT_FOR_ALL_USERS_EMAIL_SENT"),

  PASSWORD_CHANGE_ENFORCEMENT_FOR_ALL_USERS_EMAIL_FAILED(
      STUDY_DATASTORE,
      STUDY_DATASTORE,
      null,
      "Password change enforcement email failed to 1 or more users (barring superadmins).",
      "PASSWORD_CHANGE_ENFORCEMENT_FOR_ALL_USERS_EMAIL_FAILED"),

  APP_LEVEL_NOTIFICATION_LIST_VIEWED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "APP_LEVEL_NOTIFICATION_LIST_VIEWED"),

  APP_LEVEL_NOTIFICATION_CREATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "App-level notification created. Notification ID : '${notification_id}'",
      "APP_LEVEL_NOTIFICATION_CREATED"),

  APP_LEVEL_NOTIFICATION_REPLICATED_FOR_RESEND(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "App-level notification replicated for resend, by user. Existing notification ID : '${old_notification_id}', new notification ID : '${new_notification_id}'.",
      "APP_LEVEL_NOTIFICATION_REPLICATED_FOR_RESEND"),

  STUDY_LIST_VIEWED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_LIST_VIEWED"),

  STUDY_VIEWED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_VIEWED"),

  LAST_PUBLISHED_VERSION_OF_STUDY_VIEWED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "LAST_PUBLISHED_VERSION_OF_STUDY_VIEWED"),

  STUDY_ACCESSED_IN_EDIT_MODE(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_ACCESSED_IN_EDIT_MODE"),

  STUDY_PUBLISHED_AS_UPCOMING_STUDY(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_PUBLISHED_AS_UPCOMING_STUDY"),

  STUDY_LAUNCHED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_LAUNCHED"),

  STUDY_PAUSED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_PAUSED"),

  STUDY_RESUMED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_RESUMED"),

  UPDATES_PUBLISHED_TO_STUDY(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "UPDATES_PUBLISHED_TO_STUDY"),

  STUDY_DEACTIVATED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_DEACTIVATED"),

  NEW_STUDY_CREATION_INITIATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "NEW_STUDY_CREATION_INITIATED"),

  STUDY_SAVED_IN_DRAFT_STATE(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_SAVED_IN_DRAFT_STATE"),

  STUDY_BASIC_INFO_SECTION_SAVED_OR_UPDATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_BASIC_INFO_SECTION_SAVED_OR_UPDATED"),

  STUDY_BASIC_INFO_SECTION_MARKED_COMPLETE(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_BASIC_INFO_SECTION_MARKED_COMPLETE"),

  STUDY_SETTINGS_SAVED_OR_UPDATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_SETTINGS_SAVED_OR_UPDATED"),

  STUDY_SETTINGS_COMPLETED(STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_SETTINGS_COMPLETED"),

  STUDY_ENROLLMENT_SETTING_UPDATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Enrollment setting updated. Old value :'${old_enrollment_setting}', new value :'${new_enrollment_setting}'.",
      "STUDY_ENROLLMENT_SETTING_UPDATED"),

  STUDY_REJOIN_SETTING_UPDATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Re-join setting updated. Old value :'${old_rejoin_setting}', new value :'${new_rejoin_setting}'.",
      "STUDY_REJOIN_SETTING_UPDATED"),

  STUDY_DATA_RETENTION_SETTING_UPDATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Data-retention setting updated. Old value :'${old_dataretention_setting}', new value :'${new_dataretention_setting}'.",
      "STUDY_DATA_RETENTION_SETTING_UPDATED"),

  STUDY_ELIGIBILITY_SECTION_SAVED_OR_UPDATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_ELIGIBILITY_SECTION_SAVED_OR_UPDATED"),

  STUDY_ELIGIBILITY_SECTION_MARKED_COMPLETE(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_ELIGIBILITY_SECTION_MARKED_COMPLETE"),

  STUDY_CONSENT_SECTIONS_SAVED_OR_UPDATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_CONSENT_SECTIONS_SAVED_OR_UPDATED"),

  STUDY_CONSENT_SECTIONS_MARKED_COMPLETE(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_CONSENT_SECTIONS_MARKED_COMPLETE"),

  STUDY_COMPREHENSION_TEST_SECTION_SAVED_OR_UPDATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      null,
      "STUDY_COMPREHENSION_TEST_SECTION_SAVED_OR_UPDATED"),

  STUDY_COMPREHENSION_TEST_SECTION_MARKED_COMPLETE(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      null,
      "STUDY_COMPREHENSION_TEST_SECTION_MARKED_COMPLETE"),

  STUDY_DATA_SHARING_CONSENT_SETTINGS_SAVED_OR_UPDATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      null,
      "STUDY_DATA_SHARING_CONSENT_SETTINGS_SAVED_OR_UPDATED"),

  STUDY_CONSENT_DOCUMENT_SAVED_OR_UPDATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_CONSENT_DOCUMENT_SAVED_OR_UPDATED"),

  STUDY_E_CONSENT_FORM_SAVED_OR_UPDATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_E_CONSENT_FORM_SAVED_OR_UPDATED"),

  STUDY_REVIEW_AND_E_CONSENT_SAVED_OR_UPDATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_REVIEW_AND_E_CONSENT_SAVED_OR_UPDATED"),

  STUDY_REVIEW_AND_E_CONSENT_MARKED_COMPLETE(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_REVIEW_AND_E_CONSENT_MARKED_COMPLETE"),

  STUDY_CONSENT_CONTENT_NEW_VERSION_PUBLISHED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "New consent content version published with study. New consent content version: '${consent_version}'",
      "STUDY_CONSENT_CONTENT_NEW_VERSION_PUBLISHED"),

  STUDY_CONSENT_DOCUMENT_NEW_VERSION_PUBLISHED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "New consent document published for existing study. New consent document version:'${consent_document_version}'",
      "STUDY_CONSENT_DOCUMENT_NEW_VERSION_PUBLISHED"),

  STUDY_QUESTIONNAIRES_SECTION_SAVED_OR_UPDATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_QUESTIONNAIRES_SECTION_SAVED_OR_UPDATED"),

  STUDY_QUESTIONNAIRES_SECTION_MARKED_COMPLETE(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_QUESTIONNAIRES_SECTION_MARKED_COMPLETE"),

  STUDY_ACTIVE_TASK_SECTION_SAVED_OR_UPDATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_ACTIVE_TASK_SECTION_SAVED_OR_UPDATED"),

  STUDY_ACTIVE_TASK_SECTION_MARKED_COMPLETE(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_ACTIVE_TASK_SECTION_MARKED_COMPLETE"),

  STUDY_NEW_QUESTIONNAIRE_CREATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "New questionnaire created (activity ID - ${questionnaire_id}).",
      "STUDY_NEW_QUESTIONNAIRE_CREATED"),

  STUDY_QUESTIONNAIRE_SAVED_OR_UPDATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Questionnaire saved/updated (activity ID - ${questionnaire_id}).",
      "STUDY_QUESTIONNAIRE_SAVED_OR_UPDATED"),

  STUDY_QUESTIONNAIRE_MARKED_COMPLETED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Questionnaire marked completed (activity ID - ${questionnaire_id}).",
      "STUDY_QUESTIONNAIRE_MARKED_COMPLETED"),

  STUDY_INSTRUCTION_STEP_DELETED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Instruction step deleted (activity ID - ${questionnaire_id}, step ID - ${step_id}).",
      "STUDY_INSTRUCTION_STEP_DELETED"),

  STUDY_QUESTION_STEP_DELETED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Question step deleted (activity ID - ${questionnaire_id}, step ID - ${step_id}).",
      "STUDY_QUESTION_STEP_DELETED"),

  STUDY_FORM_STEP_DELETED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Form step deleted (activity ID - ${questionnaire_id}, step ID - ${step_id}).",
      "STUDY_FORM_STEP_DELETED"),

  STUDY_QUESTION_STEP_IN_FORM_DELETED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Question step in form deleted (activity ID - ${questionnaire_id}, form ID - ${form_id}, step ID - ${step_id}).",
      "STUDY_QUESTION_STEP_IN_FORM_DELETED"),

  STUDY_NEW_ACTIVE_TASK_CREATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "New Active task created (activity ID - ${activetask_id}).",
      "STUDY_NEW_ACTIVE_TASK_CREATED"),

  STUDY_ACTIVE_TASK_SAVED_OR_UPDATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Active task saved/updated (activity ID - ${activetask_id}).",
      "STUDY_ACTIVE_TASK_SAVED_OR_UPDATED"),

  STUDY_ACTIVE_TASK_MARKED_COMPLETE(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Active task marked completed (activity ID - ${activetask_id}).",
      "STUDY_ACTIVE_TASK_MARKED_COMPLETE"),

  STUDY_QUESTIONNAIRE_DELETED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Questionnaire deleted (activity ID - ${questionnaire_id}).",
      "STUDY_QUESTIONNAIRE_DELETED"),

  STUDY_ACTIVE_TASK_DELETED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Active task deleted (activity ID - ${activetask_id}).",
      "STUDY_ACTIVE_TASK_DELETED"),

  STUDY_RESOURCE_SECTION_SAVED_OR_UPDATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_RESOURCE_SECTION_SAVED_OR_UPDATED"),

  STUDY_RESOURCE_SECTION_MARKED_COMPLETE(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_RESOURCE_SECTION_MARKED_COMPLETE"),

  STUDY_NEW_RESOURCE_CREATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "New Resource created (resource ID - ${resource_id}).",
      "STUDY_NEW_RESOURCE_CREATED"),

  STUDY_RESOURCE_SAVED_OR_UPDATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Resource saved/updated (resource ID - ${resource_id}).",
      "STUDY_RESOURCE_SAVED_OR_UPDATED"),

  STUDY_RESOURCE_MARKED_COMPLETED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Resource marked done/complete (resource ID - ${resource_id}).",
      "STUDY_RESOURCE_MARKED_COMPLETED"),

  STUDY_NOTIFICATIONS_SECTION_SAVED_OR_UPDATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_NOTIFICATIONS_SECTION_SAVED_OR_UPDATED"),

  STUDY_NOTIFICATIONS_SECTION_MARKED_COMPLETE(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_NOTIFICATIONS_SECTION_MARKED_COMPLETE"),

  STUDY_NEW_NOTIFICATION_CREATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "New notification created (notification ID - ${notification_id}).",
      "STUDY_NEW_NOTIFICATION_CREATED"),

  STUDY_NOTIFICATION_SAVED_OR_UPDATED(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Notification saved/updated (notification ID - ${notification_id}).",
      "STUDY_NOTIFICATION_SAVED_OR_UPDATED"),

  STUDY_NOTIFICATION_MARKED_COMPLETE(
      STUDY_BUILDER,
      STUDY_DATASTORE,
      null,
      "Notification marked done/complete (notification ID - ${notification_id}).",
      "STUDY_NOTIFICATION_MARKED_COMPLETE"),

  STUDY_CHECKLIST_SECTION_SAVED_OR_UPDATED(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_CHECKLIST_SECTION_SAVED_OR_UPDATED"),

  STUDY_CHECKLIST_SECTION_MARKED_COMPLETE(
      STUDY_BUILDER, STUDY_DATASTORE, null, null, "STUDY_CHECKLIST_SECTION_MARKED_COMPLETE"),

  NOTIFICATION_METADATA_SENT_TO_PARTICIPANT_DATASTORE(
      STUDY_DATASTORE,
      PARTICIPANT_DATASTORE,
      null,
      "App/study notifications metadata sent.",
      "NOTIFICATION_METADATA_SENT_TO_PARTICIPANT_DATASTORE"),

  NOTIFICATION_METADATA_SEND_OPERATION_FAILED(
      STUDY_DATASTORE,
      PARTICIPANT_DATASTORE,
      null,
      "Failed to send app/study notifications metadata.",
      "NOTIFICATION_METADATA_SEND_OPERATION_FAILED"),

  STUDY_METADATA_SENT_TO_PARTICIPANT_DATASTORE(
      STUDY_DATASTORE,
      PARTICIPANT_DATASTORE,
      null,
      "App/study metadata sent.",
      "STUDY_METADATA_SENT_TO_PARTICIPANT_DATASTORE"),

  STUDY_METADATA_SEND_OPERATION_FAILED(
      STUDY_DATASTORE,
      PARTICIPANT_DATASTORE,
      null,
      "Failed to send app/study metadata.",
      "STUDY_METADATA_SEND_OPERATION_FAILED"),

  STUDY_METADATA_SENT_TO_RESPONSE_DATASTORE(
      STUDY_DATASTORE,
      RESPONSE_DATASTORE,
      null,
      "Study metadata sent.",
      "STUDY_METADATA_SENT_TO_RESPONSE_DATASTORE"),

  STUDY_METADATA_SEND_FAILED(
      STUDY_DATASTORE,
      RESPONSE_DATASTORE,
      null,
      "Failed to send study metadata.",
      "STUDY_METADATA_SEND_FAILED");

  private final PlatformComponent source;
  private final PlatformComponent destination;
  private final Optional<PlatformComponent> resourceServer;
  private final String description;
  private final String eventCode;

  StudyBuilderAuditEvent(
      PlatformComponent source,
      PlatformComponent destination,
      PlatformComponent resourceServer,
      String description,
      String eventCode) {
    this.source = source;
    this.destination = destination;
    this.resourceServer = Optional.ofNullable(resourceServer);
    this.description = description;
    this.eventCode = eventCode;
  }

  public static class Constants {

    private Constants() {}

    private static final String FMSGCWEBAPP = "FMSGCWEBAPP";

    public static final String NA = "NA";

    private static final String ACCESS_LEVEL_PLACEHOLDER = "${admin_access_level}";

    private static final String STUDY_LEVEL_ACCESS_IN_RESPONSE_DATASTORE =
        "Study-level access in Response Datastore";

    private static final String FMSGCSTUBLDR = "FMSGCSTUBLDR";

    private static final String APP_LEVEL_ACCESS_IN_PARTICIPANT_DATASTORE =
        "App-level access in Participant Datastore";

    private static final String SYSTEM_LEVEL = "System-level";

    private static final String USER_ACCESS_LEVEL = "User Access Level";

    public static final String USER_ID = "user_id";
    public static final String ACCOUNT_STATUS = "account_status";
    public static final String ACCESS_LEVEL = "access_level";
    public static final String LOCK_TIME = "lock_time";
    public static final String FAILED_ATTEMPT = "failed_attempt";
    public static final String ADMIN_USER_ID = "admin_user_id";
    public static final String EMAIL_ID = "email_id";
    public static final String ADMIN_ACCESS_LEVEL = "admin_access_lecel";
    public static final String ID = "id";
    public static final String NOTIFICATION_ID = "notification_id";
  }
}
