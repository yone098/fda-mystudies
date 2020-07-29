/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.common;

public enum PdfStorage {
  CLOUD_STORAGE(1),
  DATA_BASE(0);

  private Integer value;

  private PdfStorage(Integer value) {
    this.value = value;
  }

  public Integer value() {
    return value;
  }

  public static PdfStorage fromValue(Integer value) {
    for (PdfStorage e : PdfStorage.values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}
