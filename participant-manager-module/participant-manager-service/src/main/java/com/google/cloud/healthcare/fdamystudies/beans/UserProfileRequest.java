/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.beans;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest implements Serializable {

  private static final long serialVersionUID = 1L;
  // TODO Madhurya N (length)
  private static final String PASSWORD_REGEX =
      "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!\\\\\\\"#$%&'()*+,-.:;<=>?@\\\\\\\\[\\\\\\\\]^_`{|}~]).{8,64}$";

  @ToString.Exclude
  @NotBlank
  @Size(max = 320)
  @Email
  private String email;

  @ToString.Exclude
  @NotBlank
  @Size(
      min = 8,
      max = 64,
      message =
          "Password must contain at least 8 characters, including uppercase, lowercase letters, numbers and allowed special characters.")
  @Pattern(regexp = PASSWORD_REGEX, message = "Your password does not meet the required criteria.")
  private String currentPswd;

  @ToString.Exclude
  @NotBlank
  @Size(
      min = 8,
      max = 64,
      message =
          "Password must contain at least 8 characters, including uppercase, lowercase letters,"
              + " numbers and allowed special characters.")
  @Pattern(regexp = PASSWORD_REGEX, message = "Your password does not meet the required criteria.")
  private String newPswd;

  @ToString.Exclude
  @NotBlank
  @Size(max = 320)
  private String firstName;

  @ToString.Exclude
  @NotBlank
  @Size(max = 320)
  private String lastName;

  private String userId;
}
