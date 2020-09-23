package com.fdahpstudydesigner.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fdahpstudydesigner.common.BaseMockIT;
import com.fdahpstudydesigner.common.PathMappingUri;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

public class LoginControllerTest extends BaseMockIT {

  @Test
  public void shouldLogoutSuccessfully() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    mockMvc
        .perform(
            get(PathMappingUri.SESSION_OUT.getPath())
                .headers(headers)
                .sessionAttrs(getSessionAttributes()))
        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(view().name("login.do"));

    //    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    //    auditRequest.setUserId(userRegAdminEntity.getId());
    //    auditRequest.setAppId(appEntity.getAppId());
    //
    //    Map<String, AuditLogEventRequest> auditEventMap = new HashedMap<>();
    //    auditEventMap.put(APP_PARTICIPANT_REGISTRY_VIEWED.getEventCode(), auditRequest);
    //
    //    verifyAuditEventCall(auditEventMap, APP_PARTICIPANT_REGISTRY_VIEWED);
  }
}
