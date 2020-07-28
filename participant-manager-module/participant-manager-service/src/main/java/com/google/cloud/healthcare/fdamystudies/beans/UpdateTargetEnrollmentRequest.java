/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.beans;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTargetEnrollmentRequest {

  @NotBlank
  @Size(max = 64)
  private Integer targetEnrollment;

  @NotBlank
  @Size(max = 64)
  private String siteId;

  @NotBlank
  @Size(max = 64)
  private String studyId;

  private String userId;
}
