/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.google.cloud.healthcare.fdamystudies.common.PlatformComponent.PARTICIPANT_DATASTORE;

@Getter
@AllArgsConstructor
public enum ParticipantManagerEvent implements AuditLogEvent {
  SIGNIN_SUCCESSFUL(null, null, PARTICIPANT_DATASTORE, null, "SIGNIN_SUCCESSFUL"),

  SIGNIN_FAILED(null, null, PARTICIPANT_DATASTORE, null, "SIGNIN_FAILED"),

  SIGNIN_FAILURE_UNREGISTERED_USERNAME(
      null,
      null,
      PARTICIPANT_DATASTORE,
      "Sign-in failure due to unregistered username.",
      "SIGNIN_FAILURE_UNREGISTERED_USERNAME"),

  PASSWORD_HELP_REQUESTED(null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_HELP_REQUESTED"),

  USER_SIGNOUT_FAILURE(null, null, PARTICIPANT_DATASTORE, null, "USER_SIGNOUT_FAILURE"),

  PASSWORD_CHANGE_SUCCESS(null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_CHANGE_SUCCESS"),

  PASSWORD_CHANGE_FAILURE(null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_CHANGE_FAILURE"),

  PASSWORD_RESET_SUCCESS(null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_RESET_SUCCESS"),

  PASSWORD_RESET_FAILED(null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_RESET_FAILED"),

  PASSWORD_HELP_EMAIL_SENT(null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_HELP_EMAIL_SENT"),

  PASSWORD_RESET_EMAIL_SENT_FOR_LOCKED_ACCOUNT(
      null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_RESET_EMAIL_SENT_FOR_LOCKED_ACCOUNT"),

  PASSWORD_RESET_EMAIL_FAILURE_FOR_LOCKED_ACCOUNT(
      null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_RESET_EMAIL_FAILURE_FOR_LOCKED_ACCOUNT"),

  PASSWORD_HELP_REQUESTED_FOR_UNREGISTERED_USERNAME(
      null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_HELP_REQUESTED_FOR_UNREGISTERED_USERNAME"),

  PASSWORD_HELP_EMAIL_FAILED(null, PARTICIPANT_DATASTORE, null, null, "PASSWORD_HELP_EMAIL_FAILED"),

  NEW_USER_ACCOUNT_ACTIVATED(null, PARTICIPANT_DATASTORE, null, null, "NEW_USER_ACCOUNT_ACTIVATED"),

  NEW_USER_ACCOUNT_ACTIVATION_FAILURE(
      null, PARTICIPANT_DATASTORE, null, null, "NEW_USER_ACCOUNT_ACTIVATION_FAILURE"),

  NEW_USER_ACCOUNT_ACTIVATION_FAILURE_INVALid_ACCESS_CODE(
      null,
      PARTICIPANT_DATASTORE,
      null,
      null,
      "NEW_USER_ACCOUNT_ACTIVATION_FAILURE_INVALid_ACCESS_CODE"),

  USER_ACCOUNT_UPDATED(null, PARTICIPANT_DATASTORE, null, null, "USER_ACCOUNT_UPDATED"),

  USER_ACCOUNT_UPDATE_FAILURE(null, null, null, null, "USER_ACCOUNT_UPDATE_FAILURE"),

  ACCOUNT_LOCKED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Account locked for ${lock_time} for the user due to ${failed_attempt} consecutively failed sign-in attempts with incorrect password.",
      "ACCOUNT_LOCKED"),

  USER_SIGNOUT_SUCCESS(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "User was successfully signed out of the app.",
      "USER_SIGNOUT_SUCCESS"),

  SITE_ADDED_FOR_STUDY(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Site added successfully for a study.(Site ID- ${site_id))",
      "SITE_ADDED_FOR_STUDY"),

  PARTICIPANT_EMAIL_ADD_SUCCESS(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Participant email added successfully.(site ID- ${site_id})",
      "PARTICIPANT_EMAIL_ADD_SUCCESS"),

  PARTICIPANT_EMAIL_ADD_FAILURE(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Participant email failed to be added as email id already exist.(site ID- ${site_id})",
      "PARTICIPANT_EMAIL_ADD_FAILURE"),

  PARTICIPANTS_EMAIL_LIST_IMPORTED_SUCCESSFUL(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Participants email list imported successfully.(site ID- ${site_id})",
      "PARTICIPANTS_EMAIL_LIST_IMPORTED_SUCCESSFUL"),

  PARTICIPANTS_EMAIL_LIST_IMPORTED_FAILURE(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Participants email list failed to import.(site ID- ${site_id})",
      "PARTICIPANTS_EMAIL_LIST_IMPORTED_FAILURE"),

  SITE_DECOMMISSION_FOR_STUDY(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Site decommissioned for study successful.(site ID- ${site_id})",
      "SITE_DECOMMISSION_FOR_STUDY"),

  SITE_ACTIVATED_FOR_STUDY(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Site activated for study.(Site name- ${site_name})",
      "SITE_ACTIVATED_FOR_STUDY"),

  RESEND_INVITATION_EMAIL_SUCCESS(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Resend invitation email successful sent for participant.(site ID- ${site_id})",
      "RESEND_INVITATION_EMAIL_SUCCESS"),

  RESEND_INVITATION_EMAIL_FAILURE(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Resend invitation email failed to be sent for participant.(site ID- ${site_id})",
      "RESEND_INVITATION_EMAIL_FAILURE"),

  PARTICIPANT_INVITATION_DISABLE_SUCCESS(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Email invitation disabled successfully.(site ID- ${site_id})",
      "PARTICIPANT_INVITATION_DISABLE_SUCCESS"),

  CONSENT_DOCUMENT_DOWNLOAD_SUCCESS(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Participant consent document downloaded successful.(Consent document version - ${consent_document_version})",
      "CONSENT_DOCUMENT_DOWNLOAD_SUCCESS"),

  CONSENT_DOCUMENT_DOWNLOAD_FAILURE(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Participant consent document failed to download.(Consent document version - ${consent_document_version})",
      "CONSENT_DOCUMENT_DOWNLOAD_FAILURE"),

  INVITATION_EMAIL_SENT_SUCCESS(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Invitation email to participant sent successful.(site ID- ${site_id})",
      "INVITATION_EMAIL_SENT_SUCCESS"),

  INVITATION_EMAIL_SENT_FAILED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Invitation email to participant failed to send.(site ID- ${site_id})",
      "INVITATION_EMAIL_SENT_FAILED"),

  INVITATION_ENABLED_SUCCESS(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Participant invitation email enabled successfull(site ID- ${site_id})",
      "INVITATION_ENABLED_SUCCESS"),

  INVITATION_DISABLED_SUCCESS(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Participant invitation email disabled successfull(site ID- ${site_id})",
      "INVITATION_DISABLED_SUCCESS"),

  ENROLMENT_TARGET_SET_SUCCESS(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Enrolment target set by user successful (${target}).",
      "ENROLMENT_TARGET_SET_SUCCESS"),

  ADD_NEW_LOCATION_SUCCESS(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "New location added successful.(location ID- ${location})",
      "ADD_NEW_LOCATION_SUCCESS"),

  EDIT_LOCATION_DETAILS_SUCCESSFUL(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Location details edited successfully.(location ID - ${location_id})",
      "EDIT_LOCATION_DETAILS_SUCCESSFUL"),

  DECOMMISSION_LOCATION_SUCCESS(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Decommission of location successful (location ID - ${location_id})",
      "DECOMMISSION_LOCATION_SUCCESS"),

  DECOMMISSION_LOCATION_FAILURE(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Decommission of location failed (Location id - ${location_id})",
      "DECOMMISSION_LOCATION_FAILURE"),

  LOCATION_ACTIVATED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Location activated. (location ID- ${location_id})",
      "LOCATION_ACTIVATED"),

  NEW_USER_CREATED_SUCCESS(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "New user created successfully.(user id -{new_user_id}, User access level - {access_level})",
      "NEW_USER_CREATED_SUCCESS"),

  NEW_USER_INVITATION_EMAIL_SUCCESSFUL(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "New user invitation email sent successful.(user id- ${user_id})",
      "NEW_USER_INVITATION_EMAIL_SUCCESSFUL"),

  NEW_USER_INVITATION_EMAIL_FAILURE(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "New user invitation email sent failed.(user id- ${new_user_id})",
      "NEW_USER_INVITATION_EMAIL_FAILURE"),

  USER_DETAILS_UPDATED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "User details updated successfully(user id-{existing_user_id},user access level-{access_level})",
      "USER_DETAILS_UPDATED"),

  ACCOUNT_UPDATED_EMAIL_SENT_SUCCESSFUL(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Account updated email sent successfully(user id- ${existing_user_id})",
      "ACCOUNT_UPDATED_EMAIL_SENT_SUCCESSFUL"),

  ACCOUNT_UPDATED_EMAIL_SENT_FAILED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Account updated email failed(existing_user id- ${user_id})",
      "ACCOUNT_UPDATED_EMAIL_SENT_FAILED"),

  USER_DEACTIVATION_SUCCESSFUL(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "User account deactivated successfully (user id- ${existing_user_id}).",
      "USER_DEACTIVATION_SUCCESSFUL"),

  USER_ACTIVATED_SUCCESSFUL(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "User account activated successful (user- ${existing_user_id}).",
      "USER_ACTIVATED_SUCCESSFUL");

  private final PlatformComponent source;
  private final PlatformComponent destination;
  private final PlatformComponent resourceServer;
  private final String description;
  private final String eventCode;
}
