/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.fdahpstudydesigner.controller;

import static com.fdahpstudydesigner.common.StudyBuilderConstants.CUSTOM_STUDY_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fdahpstudydesigner.bo.ActiveTaskBo;
import com.fdahpstudydesigner.common.BaseMockIT;
import com.fdahpstudydesigner.common.PathMappingUri;
import com.fdahpstudydesigner.util.FdahpStudyDesignerConstants;
import com.fdahpstudydesigner.util.SessionObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class StudyActiveTasksControllerTest extends BaseMockIT {

  private static final String STUDY_ID_VALUE = "678574";

  private static final String CUSTOM_STUDY_ID_VALUE = "678590";

  private static final String USER_ID_VALUE = "4878641";

  @Test
  public void shouldStudyActiveTaskMarkedComplete() throws Exception {
    HttpHeaders headers = getCommonHeaders();

    SessionObject session = new SessionObject();
    session.setUserId(Integer.parseInt(USER_ID_VALUE));
    session.setStudySession(new ArrayList<>(Arrays.asList(0)));
    session.setSessionId(UUID.randomUUID().toString());

    HashMap<String, Object> sessionAttributes = getSessionAttributes();
    sessionAttributes.put(FdahpStudyDesignerConstants.SESSION_OBJECT, session);

    ActiveTaskBo activeTaskBo = new ActiveTaskBo();
    activeTaskBo.setTaskTypeId(123);
    activeTaskBo.setStudyId(678574);
    activeTaskBo.setActiveTaskFrequenciesBo(null);

    MockHttpServletRequestBuilder requestBuilder =
        post(PathMappingUri.SAVE_OR_UPDATE_ACTIVE_TASK_CONTENT.getPath())
            .headers(headers)
            .sessionAttr(CUSTOM_STUDY_ID, "OpenStudy003")
            .sessionAttrs(sessionAttributes);

    addParams(requestBuilder, activeTaskBo);
    mockMvc.perform(requestBuilder).andDo(print()).andExpect(status().isFound());
  }
}
