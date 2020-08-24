/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.fdahpstudydesigner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fdahpstudydesigner.bean.AuditLogEventRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.fdahpstudydesigner.common.JsonUtils.getObjectMapper;

@Service
public class AuditEventServiceImpl implements AuditEventService {

  @Autowired private RestTemplate restTemplate;

  private XLogger logger = XLoggerFactory.getXLogger(AuditEventServiceImpl.class.getName());

  @Override
  public void postAuditLogEvent(AuditLogEventRequest auditRequest) {
    logger.entry(
        String.format("begin postAuditLogEvent() for %s event", auditRequest.getEventCode()));

    Map<String, String> map = new HashMap<>();
    String eventsEndpoint = map.get("auditlogEventsEndpoint");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    JsonNode requestBody = getObjectMapper().convertValue(auditRequest, JsonNode.class);
    HttpEntity<JsonNode> requestEntity = new HttpEntity<>(requestBody, headers);

    ResponseEntity<JsonNode> aleResponse =
        restTemplate.exchange(eventsEndpoint, HttpMethod.POST, requestEntity, JsonNode.class);

    logger.exit(String.format("audit response=%s", aleResponse));
  }
}
