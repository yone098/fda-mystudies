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
import static com.google.cloud.healthcare.fdamystudies.common.PlatformComponent.SCIM_AUTH_SERVER;

@Getter
@AllArgsConstructor
public enum ParticipantManagerEvent implements AuditLogEvent {
  SIGNIN_SUCCESSFUL(null, null, PARTICIPANT_DATASTORE, null, "SIGNIN_SUCCESSFUL"),

  SIGNIN_FAILURE(null, null, PARTICIPANT_DATASTORE, null, "SIGNIN_FAILURE"),

  SIGNIN_FAILURE_UNREGISTERED_USER(
      null,
      null,
      PARTICIPANT_DATASTORE,
      "Sign-in failure due to unregistered username.",
      "SIGNIN_FAILURE_UNREGISTERED_USER"),

  SIGNIN_FAILURE_INVALID_PASSWORD(
      null,
      null,
      PARTICIPANT_DATASTORE,
      "Sign-in failure due to invalid password.",
      "SIGNIN_FAILURE_INVALID_PASSWORD"),

  SIGNIN_FAILURE_EXPIRED_PASSWORD(
      null,
      null,
      PARTICIPANT_DATASTORE,
      "Sign-in failure due to expired password.",
      "SIGNIN_FAILURE_EXPIRED_PASSWORD"),

  PASSWORD_HELP_REQUESTED(null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_HELP_REQUESTED"),

  PASSWORD_CHANGE_SUCCESS(null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_CHANGE_SUCCESS"),

  PASSWORD_CHANGE_FAILURE(null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_CHANGE_FAILURE"),

  PASSWORD_RESET_SUCCESS(null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_RESET_SUCCESS"),

  PASSWORD_RESET_FAILED(null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_RESET_FAILED"),

  PASSWORD_HELP_EMAIL_SENT(null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_HELP_EMAIL_SENT"),

  PASSWORD_HELP_EMAIL_FAILURE(
      null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_HELP_EMAIL_FAILURE"),

  SIGNIN_WITH_TEMPORARY_PASSWORD_SUCCESS(
      null,
      null,
      PARTICIPANT_DATASTORE,
      "User signed in with temporary password.",
      "SIGNIN_WITH_TEMPORARY_PASSWORD_SUCCESS"),

  SIGNIN_WITH_TEMPORARY_PASSWORD_FAILURE(
      null,
      null,
      PARTICIPANT_DATASTORE,
      "Sign-in with temporary password failed.",
      "SIGNIN_WITH_TEMPORARY_PASSWORD_FAILURE"),

  SIGNIN_FAILURE_INVALID_TEMPORARY_PASSWORD(
      null,
      null,
      PARTICIPANT_DATASTORE,
      "Sign-in failure due to invalid temporary password.",
      "SIGNIN_FAILURE_INVALID_TEMPORARY_PASSWORD"),

  SIGNIN_FAILURE_EXPIRED_TEMPORARY_PASSWORD(
      null,
      null,
      PARTICIPANT_DATASTORE,
      "Sign-in failure due to expired temporary password.",
      "SIGNIN_FAILURE_EXPIRED_TEMPORARY_PASSWORD"),

  PASSWORD_RESET_EMAIL_SENT_FOR_LOCKED_ACCOUNT(
      null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_RESET_EMAIL_SENT_FOR_LOCKED_ACCOUNT"),

  PASSWORD_RESET_EMAIL_FOR_LOCKED_ACCOUNT_FAILURE(
      null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_RESET_EMAIL_FOR_LOCKED_ACCOUNT_FAILURE"),

  PASSWORD_HELP_REQUESTED_FOR_UNREGISTERED_USERNAME(
      null, null, PARTICIPANT_DATASTORE, null, "PASSWORD_HELP_REQUESTED_FOR_UNREGISTERED_USERNAME"),

  USER_ACCOUNT_ACTIVATION_FAILURE(
      null, null, PARTICIPANT_DATASTORE, null, "USER_ACCOUNT_ACTIVATION_FAILURE"),

  USER_DEACTIVATED(
      null,
      null,
      PARTICIPANT_DATASTORE,
      "User account deactivated (user ID - ${edited_user_id}).",
      "USER_DEACTIVATED"),

  USER_ACCOUNT_ACTIVATED(
      null,
      null,
      PARTICIPANT_DATASTORE,
      "User account activated (user ID - ${edited_user_id}).",
      "USER_ACCOUNT_ACTIVATED"),

  ACCOUNT_LOCKED(
      null,
      null,
      PARTICIPANT_DATASTORE,
      "User account locked for ${lock_time} due to ${failed_attempt} consecutively failed sign-in attempts with incorrect password.",
      "ACCOUNT_LOCKED"),

  SIGNOUT_SUCCESSFUL(null, null, PARTICIPANT_DATASTORE, null, "SIGNOUT_SUCCESSFUL"),

  USER_SIGNOUT_FAILURE(null, null, PARTICIPANT_DATASTORE, null, "USER_SIGNOUT_FAILURE"),

  SERVICE_UNAVAILABLE_EXCEPTION(
      null,
      SCIM_AUTH_SERVER,
      PARTICIPANT_DATASTORE,
      "Failed to process request $(req_url).",
      "SERVICE_UNAVAILABLE_EXCEPTION"),

  USER_ACCOUNT_ACTIVATION_FAILURE_DUE_TO_EXPIRED_INVITATION(
      null,
      PARTICIPANT_DATASTORE,
      null,
      null,
      "USER_ACCOUNT_ACTIVATION_FAILURE_DUE_TO_EXPIRED_INVITATION"),

  SITE_ADDED_FOR_STUDY(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Site added to study (site ID- ${site_id)).",
      "SITE_ADDED_FOR_STUDY"),

  PARTICIPANT_EMAIL_ADDED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Participant email added to site (site ID- ${site_id)).",
      "PARTICIPANT_EMAIL_ADDED"),

  PARTICIPANTS_EMAIL_LIST_IMPORTED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Participants email list imported for site (site ID- ${site_id)).",
      "PARTICIPANTS_EMAIL_LIST_IMPORTED"),

  PARTICIPANTS_EMAIL_LIST_IMPORT_FAILURE(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Participants email list import failed for site (site ID- ${site_id)).",
      "PARTICIPANTS_EMAIL_LIST_IMPORT_FAILURE"),

  PARTICIPANTS_EMAIL_LIST_IMPORT_PARTIAL_FAILURE(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "1 or more emails in list failed to get imported to site (site ID- ${site_id)).",
      "PARTICIPANTS_EMAIL_LIST_IMPORT_PARTIAL_FAILURE"),

  SITE_DECOMMISSIONED_FOR_STUDY(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Site decommissioned for study (site ID- ${site_id)).",
      "SITE_DECOMMISSIONED_FOR_STUDY"),

  SITE_ACTIVATED_FOR_STUDY(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Site activated for study (site ID- ${site_id)).",
      "SITE_ACTIVATED_FOR_STUDY"),

  PARTICIPANT_INVITATION_EMAIL_RESENT(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Invitation email re-sent to 1 or more participants (site ID- ${site_id)).",
      "PARTICIPANT_INVITATION_EMAIL_RESENT"),

  PARTICIPANT_INVITATION_EMAIL_RESEND_FAILURE(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Resend of invitation email failed for 1 or more participants (site ID- ${site_id)).",
      "PARTICIPANT_INVITATION_EMAIL_RESEND_FAILURE"),

  PARTICIPANT_INVITATION_DISABLED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Invitation disabled for 1 or more participants (site ID- ${site_id)).",
      "PARTICIPANT_INVITATION_DISABLED"),

  CONSENT_DOCUMENT_DOWNLOADED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Participant consent document downloaded (site ID- ${site_id}, participant ID- ${participant_id}, consent version - ${consent_version}).",
      "CONSENT_DOCUMENT_DOWNLOADED"),

  INVITATION_EMAIL_SENT(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Invitation email sent to 1 or more participants (site ID- ${site_id)).",
      "INVITATION_EMAIL_SENT"),

  INVITATION_EMAIL_FAILURE(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Invitation email failed for 1 or more participant emails (site ID- ${site_id)).",
      "INVITATION_EMAIL_FAILURE"),

  PARTICIPANT_INVITATION_ENABLED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Invitation enabled for 1 or more participants (site ID- ${site_id)).",
      "PARTICIPANT_INVITATION_ENABLED"),

  ENROLMENT_TARGET_UPDATED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Enrolment target updated for site (site ID- ${site_id)).",
      "ENROLMENT_TARGET_UPDATED"),

  NEW_LOCATION_ADDED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "New location added (location ID- ${location}).",
      "NEW_LOCATION_ADDED"),

  LOCATION_EDITED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Location details edited (location ID - ${location_id}).",
      "LOCATION_EDITED"),

  LOCATION_DECOMMISSIONED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Location decommisioned (location ID - ${location_id})",
      "LOCATION_DECOMMISSIONED"),

  LOCATION_ACTIVATED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Location activated (location ID- ${location_id}).",
      "LOCATION_ACTIVATED"),

  NEW_USER_CREATED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "New user created (user ID - {new_user_id}, access level - {new_user_access_level}).",
      "NEW_USER_CREATED"),

  NEW_USER_INVITATION_EMAIL_SENT(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Account activation email sent to user (user ID -{new_user_id}).",
      "NEW_USER_INVITATION_EMAIL_SENT"),

  NEW_USER_INVITATION_EMAIL_FAILURE(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Account activation email to user failed (user ID -{new_user_id}).",
      "NEW_USER_INVITATION_EMAIL_FAILURE"),

  USER_DETAILS_UPDATED(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "User details updated (user id - {edited_user_id}, access level - {new_user_access_level}).",
      "USER_DETAILS_UPDATED"),

  ACCOUNT_UPDATE_EMAIL_SENT(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Account update email sent to user (user id- ${edited_user_id}).",
      "ACCOUNT_UPDATE_EMAIL_SENT"),

  ACCOUNT_UPDATE_EMAIL_FAILURE(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Account update email to user failed (user id- ${edited_user_id}).",
      "ACCOUNT_UPDATE_EMAIL_FAILURE"),

  ACCOUNT_UPDATE_BY_USER(
      null,
      PARTICIPANT_DATASTORE,
      null,
      "Account details updated by user.",
      "ACCOUNT_UPDATE_BY_USER");

  private final PlatformComponent source;
  private final PlatformComponent destination;
  private final PlatformComponent resourceServer;
  private final String description;
  private final String eventCode;
}
