/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.beans;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringExclude;

@Setter
@Getter
@NoArgsConstructor
public class LocationRequest {

  public static final String ALPHA_NUMERIC_REGEX = "^[0-9a-zA-Z]{1,15}$";

  @Size(max = 15)
  @NotBlank
  @Pattern(regexp = ALPHA_NUMERIC_REGEX, message = "Custom id does not meet the required criteria.")
  private String customId;

  @Size(max = 255)
  @NotBlank
  private String name;

  @Size(max = 255)
  @NotBlank
  private String description;

  @ToStringExclude private String userId;

  private String locationId;

  private Integer status;

  private Integer studiesCount = 0;

  private List<String> studies = new ArrayList<>();

  public LocationRequest(String customId, String name, String description) {
    this.customId = customId;
    this.name = name;
    this.description = description;
  }
}
