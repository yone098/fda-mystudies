package com.google.cloud.healthcare.fdamystudies.common;

import com.google.cloud.storage.Storage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Profile("mockit")
@Configuration
public class AppConfigTest {

  @Bean
  @Primary
  public Storage storageService() {
    return mock(Storage.class);
  }
}
