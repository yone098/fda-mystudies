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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(Include.NON_EMPTY)
// TODO Madhu, NON empty studies were coming so
public class LocationResponse extends BaseResponse {

  private String locationId;

  private String customId;

  private String description;

  private String name;

  private Integer status;

  private List<LocationRequest> locations;

  private List<String> studies = new ArrayList<>();

  public LocationResponse(ErrorCode errorCode) {
    super(errorCode);
  }

  public LocationResponse(MessageCode messageCode) {
    super(messageCode);
  }
}
