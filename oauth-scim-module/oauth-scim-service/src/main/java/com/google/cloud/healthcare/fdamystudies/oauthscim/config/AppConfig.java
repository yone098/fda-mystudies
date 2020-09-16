/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.oauthscim.config;

import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.AUTO_LOGIN_VIEW_NAME;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.ERROR_VIEW_NAME;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.LOGIN_VIEW_NAME;

import com.google.cloud.healthcare.fdamystudies.config.CommonModuleConfiguration;
import com.google.cloud.healthcare.fdamystudies.exceptions.RestResponseErrorHandler;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletContext;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

@Configuration
@Profile({"dev", "local", "qa", "mockit"})
public class AppConfig extends CommonModuleConfiguration {

  @Autowired ServletContext context;

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("home");
    registry
        .addViewController(String.format("%s/login", context.getContextPath()))
        .setViewName(LOGIN_VIEW_NAME);
    registry
        .addViewController(String.format("%s/autoLogin", context.getContextPath()))
        .setViewName(AUTO_LOGIN_VIEW_NAME);
    registry
        .addViewController(String.format("%s/error", context.getContextPath()))
        .setViewName(ERROR_VIEW_NAME);
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder)
      throws NoSuchAlgorithmException, KeyManagementException {

    TrustManager[] trustAllCerts =
        new TrustManager[] {
          new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return new X509Certificate[0];
            }

            public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
              // no implementation required
            }

            public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
              // no implementation required
            }
          }
        };
    SSLContext sslContext = SSLContext.getInstance("SSL");
    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

    int timeout = 120; // seconds
    RequestConfig config =
        RequestConfig.custom()
            .setConnectTimeout(timeout * 1000)
            .setConnectionRequestTimeout(timeout * 1000)
            .setSocketTimeout(timeout * 1000)
            .setRedirectsEnabled(false)
            .build();

    HttpClient httpClient =
        HttpClientBuilder.create()
            .setDefaultRequestConfig(config)
            .setSSLContext(sslContext)
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .disableRedirectHandling()
            .build();

    RestTemplate restTemplate =
        new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    addInterceptors(restTemplate);
    restTemplate.setErrorHandler(new RestResponseErrorHandler());
    return restTemplate;
  }
}
