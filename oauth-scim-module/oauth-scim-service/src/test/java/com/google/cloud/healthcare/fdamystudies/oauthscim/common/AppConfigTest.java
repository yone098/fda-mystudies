/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.oauthscim.common;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.healthcare.fdamystudies.config.BaseAppConfig;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

@Profile("mockit")
@Configuration
public class AppConfigTest extends BaseAppConfig {

  @Bean
  @Primary
  public JavaMailSender javaMailSender() {
    JavaMailSender javaMailSender = mock(JavaMailSender.class);
    when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
    return javaMailSender;
  }
}
