/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies;

import com.google.cloud.healthcare.fdamystudies.config.AppPropertyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {"com.google.cloud.healthcare.fdamystudies"})
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  
}
