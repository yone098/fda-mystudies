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

import static com.google.cloud.healthcare.fdamystudies.common.PlatformComponent.AUTH_SERVER;
import static com.google.cloud.healthcare.fdamystudies.common.PlatformComponent.PARTICIPANT_DATASTORE;

@Getter
@AllArgsConstructor
public enum ParticipantManagerEvent implements AuditLogEvent {
  SIGNIN_SUCCESSFUL(null, AUTH_SERVER, null, "User signed in successfully.", "SIGNIN_SUCCESSFUL"),

  SIGNIN_FAILED(null, AUTH_SERVER, null, "Sign-in attempt failed.", "SIGNIN_FAILED"),

  SIGNIN_FAILURE_UNREGISTER_USER(
      null,
      AUTH_SERVER,
      null,
      "A user attempted to sign-in with an unregistered username ${email_id}.",
      "SIGNIN_FAILURE_UNREGISTER_USER"),

  PASSWORD_HELP_REQUEST_SUCCESSFUL(
      null,
      AUTH_SERVER,
      null,
      "Password help email requested successfully.",
      "PASSWORD_HELP_REQUEST_SUCCESSFUL"),

  USER_SIGNOUT_FAILURE(
      null, AUTH_SERVER, null, "user could not be signed out of the app.", "USER_SIGNOUT_FAILURE"),

  PASSWORD_CHANGE_SUCCESS(
      PARTICIPANT_DATASTORE, null, null, "Password change successful.", "PASSWORD_CHANGE_SUCCESS"),

  PASSWORD_CHANGE_FAILURE(
      PARTICIPANT_DATASTORE, null, null, "Password change failed.", "PASSWORD_CHANGE_FAILURE"),

  PASSWORD_RESET_SUCCESS(
      PARTICIPANT_DATASTORE, null, null, "Password reset successful.", "PASSWORD_RESET_SUCCESS"),

  PASSWORD_RESET_FAILED(
      PARTICIPANT_DATASTORE, null, null, "Password reset failed.", "PASSWORD_RESET_FAILED"),

  PASSWORD_HELP_REQUEST_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Password help email request failed.",
      "PASSWORD_HELP_REQUEST_FAILURE"),

  PASSWORD_HELP_EMAIL_SENT(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Password help email sent to the user.",
      "PASSWORD_HELP_EMAIL_SENT"),

  NEW_USER_ACCOUNT_ACTIVATED(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Account activated for new user. New user record has User ID: ${user_id}, Account Status: ${account status}. User Access Level : ${access level}",
      "NEW_USER_ACCOUNT_ACTIVATED"),

  NEW_USER_ACCOUNT_ACTIVATION_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Account activation failed for new user with User ID ${user_id}. New user's Account Status: ${account status}.",
      "NEW_USER_ACCOUNT_ACTIVATION_FAILURE"),

  NEW_USER_ACCOUNT_ACTIVATION_FAILURE_INVALID_ACCESS_CODE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Account activation failed for new user with User ID ${user_id} , due to invalid Access Code. New user's Account Status: ${account status}.",
      "NEW_USER_ACCOUNT_ACTIVATION_FAILURE_INVALID_ACCESS_CODE"),

  USER_ACCOUNT_UPDATED(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Account Details updated by user in 'My Account' section",
      "USER_ACCOUNT_UPDATED"),

  USER_ACCOUNT_UPDATED_FAILED(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Attempt to update account details failed for user in 'My Account' section",
      "USER_ACCOUNT_UPDATED_FAILED"),

  SESSION_TIMEOUT_EXPIRED(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User session timed out or expired.",
      "SESSION_TIMEOUT_EXPIRED"),

  ACCOUNT_LOCKED(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Account locked for ${lock_time} for the user due to ${failed_attempt} consecutively failed sign-in attempts with incorrect password.",
      "ACCOUNT_LOCKED"),

  USER_SIGNOUT_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User was successfully signed out of the app.",
      "USER_SIGNOUT_SUCCESS"),

  SEARCH_FOR_SITE_STUDY_ID_OR_NAME_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User search for site, study or name in dashboard successful.",
      "SEARCH_FOR_SITE_STUDY_ID_OR_NAME_SUCCESS"),

  SEARCH_FOR_SITE_STUDY_ID_OR_NAME_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User search for site, study or name in dashboard failed.",
      "SEARCH_FOR_SITE_STUDY_ID_OR_NAME_FAILURE"),

  SITE_ADDED_FOR_STUDY(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User successfully added site.(Site ${site}, Study -${study})",
      "SITE_ADDED_FOR_STUDY"),

  SITE_ADDED_FOR_STUDY_FAILED(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User added site for study failed as site already exist.(Site ${site}, Study -${study}, App -${app_name})",
      "SITE_ADDED_FOR_STUDY_FAILED"),

  PARTICIPANT_EMAIL_ADD_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Participant email added successfully.(Site-${site}, Study-${study_name}, App-${app_name},Email - ${email_id})",
      "PARTICIPANT_EMAIL_ADD_SUCCESS"),

  PARTICIPANT_EMAIL_ADD_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Participant email failed to be added as email id already exist.(Site-${site}, Study-${study_name}, App-${app_name},Email id - ${email_id})",
      "PARTICIPANT_EMAIL_ADD_FAILURE"),

  PARTICIPANTS_EMAIL_LIST_IMPORTED_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Participants email list imported successfully.(Site-${site}, Study-${study_name}, App-${app_name})",
      "PARTICIPANTS_EMAIL_LIST_IMPORTED_SUCCESSFUL"),

  PARTICIPANTS_EMAIL_LIST_IMPORTED_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Participants email list failed to import.(Site-${site}, Study-${study_name}, App-${app_name})",
      "PARTICIPANTS_EMAIL_LIST_IMPORTED_FAILURE"),

  SITE_DECOMMISSION_FOR_STUDY(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Site decommissioned for study successful.(Site-${site}, Study-${study_name}, App-${app_name})",
      "SITE_DECOMMISSION_FOR_STUDY"),

  SITE_DECOMMISSION_FOR_STUDY_FAILURE_DUE_TO_ENROLLED_PARTICIPANTS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Site decommissioned for study failed as participants exist at site level.(Site-${site}, Study-${study_name}, App-${app_name})",
      "SITE_DECOMMISSION_FOR_STUDY_FAILURE_DUE_TO_ENROLLED_PARTICIPANTS"),

  SITE_DECOMMISSION_FOR_STUDY_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Site decommissioned for study failure.(Site-${site},Study-${study_name}, App-${app_name})",
      "SITE_DECOMMISSION_FOR_STUDY_FAILURE"),

  DOWNLOAD_IMPORT_TEMPLATE_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User attempted to download import template success.(Site-${site}, Study-${study_name}, App-${app_name})",
      "DOWNLOAD_IMPORT_TEMPLATE_SUCCESS"),

  DOWNLOAD_IMPORT_TEMPLATE_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User attempted to download import template failed (Site-${site}, Study-${study_name}, App-${app_name})",
      "DOWNLOAD_IMPORT_TEMPLATE_FAILURE"),

  PARTICIPANT_DETAILS_ACCESS_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User successfully accessed Participant details.(Site- ${site}, Study-${study_name}, App-${app_name},Email id - ${email_id} ,Enrolment Status -${enrolment_status}, Date of enrolment ${date_of_enrolment},Date of withdrawal ${date_of_withdrawal},Onboarding Status ${onboarding_status}, Last Invitation Sent ${last_invitation_date})",
      "PARTICIPANT_DETAILS_ACCESS_SUCCESS"),

  PARTICIPANT_DETAILS_ACCESS_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User failed to access Participant details.(Site- ${site}, Study-${study_name}, App-${app_name},Email id - ${email_id},Enrolment Status -${enrolment_status}, Date of enrolment ${date_of_enrolment},Date of withdrawal ${date_of_withdrawal},Onboarding Status ${onboarding_status}, Last Invitation Sent ${last_invitation_date})",
      "PARTICIPANT_DETAILS_ACCESS_FAILURE"),

  RESEND_INVITATION_FOR_PARTICIPANT_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Resend study invitation email successful to participant.(Email - ${email_id},Site-${site}, Study-${study_name}, App-${app_name}, Enrolment Status -${enrolment_status}, Last Invited date ${last_invited_date})",
      "RESEND_INVITATION_FOR_PARTICIPANT_SUCCESS"),

  RESEND_INVITATION_FOR_PARTICIPANT_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Resend study invitation email failed to participant.(Email - ${email_id},Site-${site}, Study-${study_name}, App-${app_name}, Enrolment Status -${enrolment_status},Last Invited date ${last_invited_date})",
      "RESEND_INVITATION_FOR_PARTICIPANT_FAILURE"),

  DISABLE_INVITATION_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Participant study invitation disabled successfully.(Email - ${email_id},Site-${site}, Study-${study_name}, App-${app_name})",
      "DISABLE_INVITATION_SUCCESS"),

  DISABLE_INVITATION_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Participant study invitation failed to disable.(Email - ${email_id},Site-${site}, Study-${study_name}, App-${app_name})",
      "DISABLE_INVITATION_FAILURE"),

  CONSENT_DOCUMENT_DOWNLOAD_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Participant consent document downloaded successful.(Consent document version - ${document_version},Data sharing permission- ${data_sharing_permission}, Site-${site}, Study-${study_name}, App-${app_name})",
      "CONSENT_DOCUMENT_DOWNLOAD_SUCCESS"),

  CONSENT_DOCUMENT_DOWNLOAD_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Participant consent document failed to download.(Consent document version - ${Consent_document_version},Data sharing permission- ${data_sharing_permission},Site-${site}, Study-${study_name}, App-${app_name})",
      "CONSENT_DOCUMENT_DOWNLOAD_FAILURE"),

  STUDY_INVITE_SENT_FOR_PARTICIPANT_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Study invitation email to participant sent successful.(Site-${site}, Study-${study_name}, App-${app_name}, Email id(s)-{email_id})",
      "STUDY_INVITE_SENT_FOR_PARTICIPANT_SUCCESS"),

  STUDY_INVITE_SENT_FOR_PARTICIPANTS_FAILED(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Study invitation email to participant sent failed.(Site-${site}, Study-${study_name}, App-${app_name}.Status-${status}, Email id(s)-${email_id})",
      "STUDY_INVITE_SENT_FOR_PARTICIPANTS=_FAILED"),

  STUDY_INVITATION_ENABLED_FOR_PARTICIPANT_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Participant study invitation email enabled successful.(Study-${study_name}, App-${app_name},Email id -${email_id}, Date of disabled -${disabled_date}, Enrolment Status -${enrolment_status})",
      "STUDY_INVITATION_ENABLED_FOR_PARTICIPANT_SUCCESSFUL"),

  STUDY_INVITATION_ENABLE_PARTICIPANTS_FAILED(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Participant study invitation email failed to enable.(Study-${study_name}, App-${app_name}, Email id ${email_id}, Date of disabled -${disabled_date}, Enrolment Status -${enrolment_status})",
      "STUDY_INVITATION_ENABLE_PARTICIPANTS_FAILED"),

  ENROLMENT_TARGET_SET_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Enrolment target set by user successful (${target}).",
      "ENROLMENT_TARGET_SET_SUCCESS"),

  ENROLMENT_TARGET_SET_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Enrolment target set by user failed (${target}).",
      "ENROLMENT_TARGET_SET_FAILURE"),

  SEARCH_SITE_ID_OR_PARTICIPANT_EMAIL_ID(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User search by site id or participant email id in study participant registery successful.(Email - ${par_email_id}, Study-${study_name})",
      "SEARCH_SITE_ID_OR_PARTICIPANT_EMAIL_ID"),

  SEARCH_SITE_ID_OR_PARTICIPANT_EMAIL_ID_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User search by site id or participant email id in study participant registery failed.(Email - ${par_email_id},Site-${site}, Study-${study_name})",
      "SEARCH_SITE_ID_OR_PARTICIPANT_EMAIL_ID_FAILURE"),

  SEARCH_PARTICIPANT_EMAIL_ID_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User search for participant email id in app registry successful.(Email - ${par_email_id})",
      "SEARCH_PARTICIPANT_EMAIL_ID_SUCCESS"),

  SEARCH_PARTICIPANT_EMAIL_ID_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User search for participant email id in app registry failed.(Email - ${par_email_id})",
      "SEARCH_PARTICIPANT_EMAIL_ID_FAILURE"),

  SEARCH_LOCATION_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User search for location successful.(location Name- ${location})",
      "SEARCH_LOCATION_SUCCESS"),

  SEARCH_LOCATION_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User search for location failure (location Name- ${location})",
      "SEARCH_LOCATION_FAILURE"),

  ADD_NEW_LOCATION_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "New location added by user successful.(location Name- ${location})",
      "ADD_NEW_LOCATION_SUCCESS"),

  ADD_NEW_LOCATION_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "New location added by user failed.(location Name- ${location})",
      "ADD_NEW_LOCATION_FAILURE"),

  LOCATION_DETAILS_SELECT_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Location details from location listing selected successfully.(location Name- ${location}, Location Status -${location_Status}, Associated Studies -${studies_associated})",
      "LOCATION_DETAILS_SELECT_SUCCESS"),

  LOCATION_DETAILS_SELECT_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Location details from location listing failed to load.(location Name- ${location}, Location Status -${location_Status}, Associated Studies -${studies_associated})",
      "LOCATION_DETAILS_SELECT_FAILURE"),

  EDIT_LOCATION_DETAILS_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Location details edited successfully.(location Name- ${location},Location ID - ${location_id}, Location Status -${location_Status})",
      "EDIT_LOCATION_DETAILS_SUCCESSFUL"),

  EDIT_LOCATION_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Location details edited by user failed.(location Name- ${location},Location ID - ${location_id}, Location Status -${location_Status})",
      "EDIT_LOCATION_FAILURE"),

  DECOMMISSION_LOCATION_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Decommission of location successful (location Name- ${location},Location ID - ${location_id}, Location Status -${location_Status})",
      "DECOMMISSION_LOCATION_SUCCESS"),

  DECOMMISSION_LOCATION_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Decommission of location failed (location Name- ${location}, Location ID - ${location_id},Location Status -${location_Status})",
      "DECOMMISSION_LOCATION_FAILURE"),

  SEARCH_BY_USER_NAME_OR_EMAIL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User search for user name or email in users page success.(${user_id} , email - ${email_id})",
      "SEARCH_BY_USER_NAME_OR_EMAIL"),

  SEARCH_BY_USER_NAME_OR_EMAIL_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User search for user name or email in users page failed.(${user_id} , email - ${email_id})",
      "SEARCH_BY_USER_NAME_OR_EMAIL_FAILURE"),

  NEW_USER_BASIC_DETAILS_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Basic details of new user captured successful.(New user- ${user_id}, email - ${email_id}, Role - ${role}, Status- ${status})",
      "NEW_USER_BASIC_DETAILS_SUCCESS"),

  NEW_USER_BASIC_DETAILS_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "New user basic details captured failed.(new user- ${user_id}, email - ${email_id}, Role - ${role}, Status- ${status})",
      "NEW_USER_BASIC_DETAILS_FAILURE"),

  NEW_USER_LOCATION_PERMISSION_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Permission location of newly added user set successful.(new user- ${user_id}, email - ${email_id}, Role - ${role}, location permission - ${location_permission)}",
      "NEW_USER_LOCATION_PERMISSION_SUCCESSFUL"),

  NEW_USER_LOCATION_PERMISSION_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Permission location of newly added user failed.(new user- ${user_id}, email - ${email_id}, Role - ${role}, Status- ${status}",
      "NEW_USER_LOCATION_PERMISSION_FAILURE"),

  NEW_USER_APP_DETAILS_ADDED_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "App details added successfully for new user.(new user- ${user_id}, email - ${email_id}, Role - ${role}, Status- ${status}, App associated - ${apps}, App ID - ${app_id})",
      "NEW_USER_APP_DETAILS_ADDED_SUCCESSFUL"),

  NEW_USER_APP_DETAILS_ADDED_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "App details failed to be add for new user.(new user- ${user_id}, email - ${email_id}, Role - ${role}, Status- ${status}, App associated - ${apps}, App ID - ${app_id})",
      "NEW_USER_APP_DETAILS_ADDED_FAILURE"),

  NEW_USER_APP_PERMISSION_SET_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "App permission set successful for the new user.(new user- ${user_id}, email - ${email_id}, Role - ${role}, Status- ${status}, App associated - ${apps}, App ID - ${app_id}, App permission- ${app_permission})",
      "NEW_USER_APP_PERMISSION_SET_SUCCESSFUL"),

  NEW_USER_APP_PERMISSION_SET_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "App permission set failed for new user.(new user- ${user_id}, email - ${email_id}, Role - ${role}, App ID - ${app_id}, App permission- ${app_permission})",
      "NEW_USER_APP_PERMISSION_SET_FAILURE"),

  NEW_USER_STUDY_PERMISSION_SET_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Study permission set successful for new user.(new user- ${user_id}, email - ${email_id}, Role - ${role}, App ID - ${app_id}, App permission- ${app_permission}, Study Permission -${study_permission},Study name - ${study_name})",
      "NEW_USER_STUDY_PERMISSION_SET_SUCCESSFUL"),

  NEW_USER_STUDY_PERMISSION_SET_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Study permission failed for new user.(user- ${user_id}, email - ${email_id}, Role - ${role}, App ID - ${app_id}, App permission- ${app_permission}, Study Permission -${study_permission},Study name - ${study_name})",
      "NEW_USER_STUDY_PERMISSION_SET_FAILURE"),

  NEW_USER_SITE_PERMISSION_SET_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Site permission set successfully for new user.(new user- ${user_id}, email - ${email_id}, Role - ${role}, App ID - ${app_id},Study name - ${study_name}, Site name - ${site_name}, Site permission- ${site_permission})",
      "NEW_USER_SITE_PERMISSION_SET_SUCCESSFUL"),

  NEW_USER_SITE_PERMISSION_SET_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Site permission failed to be set for new user. (new user- ${user_id}, email - ${email_id}, Role - ${role}, App ID - ${app_id},Study name - ${study_name}, Site name - ${site_name}, Site permission- ${site_permission})",
      "NEW_USER_SITE_PERMISSION_SET_FAILURE"),

  NEW_USER_CREATED_SUCCESS(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "New user created created successfully.(User id- ${user_id}, Email id- ${email_id})",
      "NEW_USER_CREATED_SUCCESS"),

  NEW_USER_CREATED_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "New user creation failed.(User id- ${user_id}, Email id- ${email_id})",
      "NEW_USER_CREATED_FAILURE"),

  NEW_USER_INVITATION_EMAIL_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User invitation email sent successful.(User id- ${user_id}, Email id- ${email_id})",
      "NEW_USER_INVITATION_EMAIL_SUCCESSFUL"),

  NEW_USER_INVITATION_EMAIL_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User invitation email sent failed.(User id- ${user_id}, Email id- ${email_id})",
      "NEW_USER_INVITATION_EMAIL_FAILURE"),

  USER_DETAILS_VIEWED_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User details viewed successfully.(user- ${user_id}, email - ${email_id}, Role - ${role}, Status- ${status}, App associated - ${apps}).",
      "USER_DETAILS_VIEWED_SUCCESSFUL"),

  USER_DETAILS_VIEWED_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User details viewed failed.(user- ${user_id}, email - ${email_id}, Role - ${role}, Status- ${status}, App associated - ${apps}).",
      "USER_DETAILS_VIEWED_FAILURE"),

  USER_ROLE_UPDATED_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User role updated successful. (user- ${user_id}, email - ${email_id}, Role changed from ${old_role} to ${new_role}, Status- ${status}, App associated - ${apps}).",
      "USER_ROLE_UPDATED_SUCCESSFUL"),

  USER_ROLE_UPDATED_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User role failed to update.(user- ${user_id}, email - ${email_id}, Role -{role},Status- ${status}, App associated - ${apps}).",
      "USER_ROLE_UPDATED_FAILURE"),

  USER_LOCATION_PERMISSION_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Location permission for user added successful (user- ${user_id}, email - ${email_id}, Role - ${role}, location permission - ${location_permission)}",
      "USER_LOCATION_PERMISSION_SUCCESSFUL"),

  USER_LOCATION_PERMISSION_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Location permission failed to add for user (user- ${user_id}, email - ${email_id}, Role - ${role}, Status- ${status}",
      "USER_LOCATION_PERMISSION_FAILURE"),

  USER_APP_ADDED_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "App added successfully for user (user- ${user_id}, email - ${email_id}, Role - ${role}, Status- ${status}, App associated - ${apps}, App ID - ${app_id})",
      "USER_APP_ADDED_SUCCESSFUL"),

  USER_APP_ADDED_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "App failed to add for user (user- ${user_id}, email - ${email_id}, Role - ${role}, Status- ${status}, App associated - ${apps}, App ID - ${app_id})",
      "USER_APP_ADDED_FAILURE"),

  USER_APP_DELETED_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "App associated for the user deleted failure (user- ${user_id}, email - ${email_id}, Role - ${role}, Status- ${status}, App associated - ${apps}, App ID - ${app_id}).",
      "USER_APP_DELETED_SUCCESSFUL"),

  USER_APP_PERMISSION_UPDATE_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "App permission update successful for the user (user- ${user_id}, email - ${email_id}, Role - ${role}, Status- ${status}, App associated - ${apps}, App ID - ${app_id}, App permission- ${app_permission})",
      "USER_APP_PERMISSION_UPDATE_SUCCESSFUL"),

  USER_APP_PERMISSION_UPDATE_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "App permission update failed for user (user- ${user_id}, email - ${email_id}, Role - ${role}, App ID - ${app_id}, App permission- ${app_permission})",
      "USER_APP_PERMISSION_UPDATE_FAILURE"),

  USER_STUDY_SELECTED_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Study selected for user successful (user- ${user_id}, email - ${email_id}, Role - ${role}, App ID - ${app_id},Study Permission -${study_permission},Study name - ${study_name})",
      "USER_STUDY_SELECTED_SUCCESSFUL"),

  USER_STUDY_SELECTED_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Study selected for user failed (user- ${user_id}, email - ${email_id}, Role - ${role}, App ID - ${app_id},Study Permission -${study_permission},Study name - ${study_name})",
      "USER_STUDY_SELECTED_FAILURE"),

  USER_STUDY_PERMISSION_UPDATE_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Study permission updated successful for user (user- ${user_id}, email - ${email_id}, Role - ${role}, App ID - ${app_id},Study name - ${study_name}), Study Permission -${study_permission}",
      "USER_STUDY_PERMISSION_UPDATE_SUCCESSFUL"),

  USER_STUDY_PERMISSION_UPDATE_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Study permission updation failed for user (user- ${user_id}, email - ${email_id}, Role - ${role}, App ID - ${app_id},Study name - ${study_name}), Study Permission -${study_permission}",
      "USER_STUDY_PERMISSION_UPDATE_FAILURE"),

  USER_SITE_SELECTED_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Site selected for user failed (user- ${user_id}, email - ${email_id}, Role - ${role}, App ID - ${app_id},Study name - ${study_name}, Site name - ${site_name}, Site permission- ${site_permission})",
      "USER_SITE_SELECTED_SUCCESSFUL"),

  USER_SITE_PERMISSION_UPDATE_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Site permission set successfully for new user- (user- ${user_id}, email - ${email_id}, Role - ${role}, App ID - ${app_id},Study name - ${study_name}, Site name - ${site_name}, Site permission- ${site_permission})",
      "USER_SITE_PERMISSION_UPDATE_SUCCESSFUL"),

  USER_SITE_PERMISSION_UPDATE_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Site permission failed for new user- (user- ${user_id}, email - ${email_id}, Role - ${role}, App ID - ${app_id},Study name - ${study_name}, Site name - ${site_name}, Site permission- ${site_permission})",
      "USER_SITE_PERMISSION_UPDATE_FAILURE"),

  USER_ACCOUNT_DEACTIVATION_SUCCESSFUL(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User account deactivated successfully (user- ${user_id}, email - ${email_id}).",
      "USER_ACCOUNT_DEACTIVATION_SUCCESSFUL"),

  USER_ACCOUNT_DEACTIVATED_FAILURE(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User account deactivation failed (user- ${user_id}, email - ${email_id}).",
      "USER_ACCOUNT_DEACTIVATED_FAILURE"),

  USER_ACCOUNT_ACTIVATED(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "User account reactivated successful (user- ${user_id}, email - ${email_id}).",
      "USER_ACCOUNT_ACTIVATED"),

  LOCATION_ACTIVATED(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Location activated. (Location Name ${location_name})",
      "LOCATION_ACTIVATED"),

  SITE_ACTIVATED(
      PARTICIPANT_DATASTORE,
      null,
      null,
      "Site activated for study.(Study ${study_name}, Site name - ${site_name})",
      "SITE_ACTIVATED");

  private final PlatformComponent source;
  private final PlatformComponent destination;
  private final PlatformComponent resourceServer;
  private final String description;
  private final String eventCode;
}
