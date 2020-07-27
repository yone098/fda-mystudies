/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.beans;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ManageUserAppBean {

  private String id;

  private String customId;

  private String name;

  private boolean selected = false;

  private boolean disabled = true;

  private int permission;

  private boolean viewApp;

  private int totalStudiesCount = 0;

  private int selectedStudiesCount = 0;

  private int totalSitesCount = 0;

  private int selectedSitesCount = 0;

  private List<StudiesResponseBean> studies;
}
