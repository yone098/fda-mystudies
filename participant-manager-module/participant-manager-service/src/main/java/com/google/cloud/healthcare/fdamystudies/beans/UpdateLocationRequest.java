/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.beans;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.ToStringExclude;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class UpdateLocationRequest {

  public static final String ALPHA_NUMERIC_REGEX = "^[0-9a-zA-Z]{1,15}$";

  @Size(max = 255)
  private String name;

  @Size(max = 255)
  private String description;

  @ToStringExclude private String userId;

  private String locationId;

  @Min(0)
  @Max(1)
  private Integer status;

  public UpdateLocationRequest(String name, String description) {
    this.name = name;
    this.description = description;
  }
}
