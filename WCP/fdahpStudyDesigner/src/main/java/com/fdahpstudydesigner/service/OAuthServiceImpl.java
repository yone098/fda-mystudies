/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.fdahpstudydesigner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fdahpstudydesigner.util.FdahpStudyDesignerUtil;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class OAuthServiceImpl extends BaseServiceImpl implements OAuthService {

  private XLogger logger = XLoggerFactory.getXLogger(OAuthServiceImpl.class.getName());

  private static final String SCOPE = "scope";

  private static final String AUTHORIZATION = "Authorization";

  private static final String CONTENT_TYPE = "Content-Type";

  private static final String GRANT_TYPE = "grant_type";

  private static final String REDIRECT_URI = "redirect_uri";

  private static final String ACCESS_TOKEN = "access_token";

  private static final String APPLICATION_X_WWW_FORM_URLENCODED_CHARSET_UTF_8 =
      "application/x-www-form-urlencoded;charset=UTF-8";

  private String accessToken;

  private String encodedAuthorization;

  Map<String, String> map = new HashMap<>();

  @PostConstruct
  public void init() {
    map = FdahpStudyDesignerUtil.getAppProperties();

    String clientId = map.get("security.oauth2.client.client-id");
    String clientSecret = map.get("security.oauth2.client.client-secret");
    String credentials = clientId + ":" + clientSecret;
    encodedAuthorization = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
  }

  @Override
  public String getAccessToken() {
    if (StringUtils.isEmpty(accessToken)) {
      this.accessToken = getNewAccessToken();
    }
    return this.accessToken;
  }

  @Override
  public String getNewAccessToken() {
    logger.entry("begin getNewAccessToken()");
    ResponseEntity<JsonNode> response = getToken();
    if (isSuccessful(response)) {
      this.accessToken = response.getBody().get(ACCESS_TOKEN).textValue();
      logger.exit(String.format("status=%d", response.getStatusCode().value()));
    } else {
      logger.error(
          String.format(
              "Get new access token from oauth scim service failed with status=%d and response=%s",
              response.getStatusCode().value(), response.getBody().toString()));
    }
    return this.accessToken;
  }

  private ResponseEntity<JsonNode> getToken() {
    map = FdahpStudyDesignerUtil.getAppProperties();

    String tokenEndpoint = map.get("security.oauth2.token_endpoint");
    String clientRedirectUri = map.get("security.oauth2.client.redirect-uri");

    HttpHeaders headers = new HttpHeaders();
    headers.add(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED_CHARSET_UTF_8);
    headers.add(AUTHORIZATION, encodedAuthorization);

    MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
    requestBody.add(GRANT_TYPE, "client_credentials");
    requestBody.add(SCOPE, "openid");
    requestBody.add(REDIRECT_URI, clientRedirectUri);

    ResponseEntity<JsonNode> response =
        exchangeForJson(tokenEndpoint, headers, requestBody, HttpMethod.POST);

    if (!isSuccessful(response)) {
      logger.error(
          String.format(
              "get token failed with status %d and response %s",
              response.getStatusCode().value(), response.getBody().toString()));
    }

    return response;
  }
}
