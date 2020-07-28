/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.common;

import java.io.IOException;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@JsonSerialize(using = MessageCode.MessageCodeSerializer.class)
public enum MessageCode {
  ADD_SITE_SUCCESS(HttpStatus.CREATED, "MSG-0001", "Site added successfully"),

  ADD_LOCATION_SUCCESS(HttpStatus.CREATED, "MSG-0002", "New location added successfully"),

  GET_APPS_SUCCESS(HttpStatus.OK, "MSG-0003", "Get apps successfully"),

  GET_STUDIES_SUCCESS(HttpStatus.OK, "MSG-0004", "Get studies successfull"),

  DECOMMISSION_SUCCESS(HttpStatus.OK, "MSG-0005", "Decommission successfully"),

  REACTIVE_SUCCESS(HttpStatus.OK, "MSG-0006", "Reactivate successfully"),

  LOCATION_UPDATE_SUCCESS(HttpStatus.OK, "MSG-0007", "Location updated successfully"),

  GET_LOCATION_SUCCESS(HttpStatus.OK, "MSG-0008", "Get locations successfull"),

  GET_LOCATION_FOR_SITE_SUCCESS(HttpStatus.OK, "MSG-0009", "Get locations for site successfull"),

  GET_USER_PROFILE_SUCCESS(HttpStatus.OK, "MSG-0010", "Get user profile successfull"),

  PROFILE_UPDATED_SUCCESS(HttpStatus.OK, "MSG-0011", "Profile updated successfully"),

  GET_USER_PROFILE_WITH_SECURITY_CODE_SUCCESS(
      HttpStatus.OK, "MSG-0012", "Get user profile with security code successfull"),

  GET_PARTICIPANT_REGISTRY_SUCCESS(
      HttpStatus.OK, "MSG-0013", "Get participant registry successfull"),

  DECOMMISSION_SITE_SUCCESS(HttpStatus.OK, "MSG-0014", "Site Decommissioned successfully"),

  RECOMMISSION_SITE_SUCCESS(HttpStatus.OK, "MSG-0015", "Site Recommissioned successfully"),

  PARTICIPANTS_INVITED_SUCCESS(HttpStatus.OK, "MSG-0006", "participants are invited"),

  ADD_PARTICIPANT_SUCCESS(HttpStatus.CREATED, "MSG-0016", "Participant added successfully"),

  GET_SITES_SUCCESS(HttpStatus.OK, "MSG-0017", "Get sites successfully"),

  GET_APPS_DETAILS_SUCCESS(HttpStatus.OK, "MSG-0018", "Get App Details successfull"),

  GET_PARTICIPANT_DETAILS_SUCCESS(
      HttpStatus.OK, "MSG-0019", "Get participant details successfully"),

  GET_APP_PARTICIPANTS_SUCCESS(HttpStatus.OK, "MSG-0005", "get App Participants successfully"),

  EMAIL_ACCEPTED_BY_MAIL_SERVER(
      HttpStatus.ACCEPTED, "MSG-0003", "The email is accepted by the receiving mail server."),

  GET_CONSENT_DOCUMENT_SUCCESS(HttpStatus.OK, "MSG-0005", "Get consent document successfully"),

  ADD_NEW_USER_SUCCESS(HttpStatus.CREATED, "MSG-0020", "New user added successfully"),

  UPDATE_USER_SUCCESS(HttpStatus.OK, "MSG-0021", "User updated successfully"),

  IMPORT_PARTICIPANT_SUCCESS(HttpStatus.OK, "MSG-0021", "Participants imported successfully"),

  PARTICIPANT_ENABLED(HttpStatus.OK, "MSG-0022", "Participants are enabled"),

  PARTICIPANT_DISABLED(HttpStatus.OK, "MSG-0023", "Participants are disabled"),

  MANAGE_USERS_SUCCESS(HttpStatus.OK, "MSG-0024", "manage users successfull"),

  TARGET_ENROLLMENT_UPDATE_SUCCESS(
      HttpStatus.OK, "MSG-0014", "Target enrolllment updated successfully");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  static class MessageCodeSerializer extends StdSerializer<MessageCode> {

    private static final long serialVersionUID = 1L;

    public MessageCodeSerializer() {
      super(MessageCode.class);
    }

    @Override
    public void serialize(
        MessageCode msgCode, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
        throws IOException {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeNumberField("status", msgCode.getHttpStatus().value());
      jsonGenerator.writeStringField("code", msgCode.getCode());
      jsonGenerator.writeStringField("message", msgCode.getMessage());
      jsonGenerator.writeEndObject();
    }
  }
}
