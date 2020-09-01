package com.google.cloud.healthcare.fdamystudies.controller.tests;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.healthcare.fdamystudies.bean.StudyReqBean;
import com.google.cloud.healthcare.fdamystudies.beans.DeactivateAcctBean;
import com.google.cloud.healthcare.fdamystudies.beans.InfoBean;
import com.google.cloud.healthcare.fdamystudies.beans.LoginBean;
import com.google.cloud.healthcare.fdamystudies.beans.SettingsRespBean;
import com.google.cloud.healthcare.fdamystudies.beans.UserRequestBean;
import com.google.cloud.healthcare.fdamystudies.common.BaseMockIT;
import com.google.cloud.healthcare.fdamystudies.controller.UserProfileController;
import com.google.cloud.healthcare.fdamystudies.service.FdaEaUserDetailsServiceImpl;
import com.google.cloud.healthcare.fdamystudies.service.UserManagementProfileService;
import com.google.cloud.healthcare.fdamystudies.testutils.Constants;
import com.google.cloud.healthcare.fdamystudies.testutils.TestUtils;
import com.google.cloud.healthcare.fdamystudies.usermgmt.model.UserDetailsBO;
import com.google.cloud.healthcare.fdamystudies.util.EmailNotification;
import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MvcResult;

public class UserProfileControllerTest extends BaseMockIT {

  private static final String PING_PATH = "/ping";

  private static final String USER_PROFILE_PATH = "/myStudiesUserMgmtWS/userProfile";

  private static final String UPDATE_USER_PROFILE_PATH = "/myStudiesUserMgmtWS/updateUserProfile";

  private static final String DEACTIVATE_PATH = "/myStudiesUserMgmtWS/deactivate";

  private static final String RESEND_CONFIRMATION_PATH = "/myStudiesUserMgmtWS/resendConfirmation";

  @Autowired private UserProfileController profileController;

  @Autowired private UserManagementProfileService profileService;

  @Autowired private FdaEaUserDetailsServiceImpl service;

  @Autowired private EmailNotification emailNotification;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private JavaMailSender emailSender;

  @Value("${response.server.url.participant.withdraw}")
  private String withdrawUrl;

  @Test
  public void contextLoads() {
    assertNotNull(profileController);
    assertNotNull(mockMvc);
    assertNotNull(profileService);
    assertNotNull(service);
  }

  @Test
  public void ping() throws Exception {
    mockMvc
        .perform(get(PING_PATH).headers(TestUtils.getCommonHeaders(Constants.USER_ID_HEADER)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  public void getUserProfileSuccess() throws Exception {
    HttpHeaders headers = TestUtils.getCommonHeaders();
    headers.set(Constants.USER_ID_HEADER, Constants.VALID_USER_ID_1);
    mockMvc
        .perform(get(USER_PROFILE_PATH).headers(headers).contextPath(getContextPath()))
        .andDo(print())
        .andExpect(content().string(containsString("abc@gmail.com")))
        .andExpect(status().isOk());

    verifyTokenIntrospectRequest(1);
  }

  @Test
  public void getUserProfileBadRequest() throws Exception {
    HttpHeaders headers = TestUtils.getCommonHeaders(Constants.USER_ID_HEADER);

    // Invalid userId
    headers.set(Constants.USER_ID_HEADER, Constants.INVALID_USER_ID);
    mockMvc
        .perform(get(USER_PROFILE_PATH).headers(headers).contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest());

    verifyTokenIntrospectRequest(1);
  }

  @Test
  public void updateUserProfileSuccess() throws Exception {
    HttpHeaders headers = TestUtils.getCommonHeaders(Constants.USER_ID_HEADER);

    SettingsRespBean settingRespBean = new SettingsRespBean(true, true, true, true, "", "");
    UserRequestBean userRequestBean = new UserRequestBean(settingRespBean, new InfoBean());
    String requestJson = getObjectMapper().writeValueAsString(userRequestBean);
    mockMvc
        .perform(
            post(UPDATE_USER_PROFILE_PATH)
                .content(requestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(String.valueOf(HttpStatus.OK.value()))));

    verifyTokenIntrospectRequest(1);

    MvcResult result =
        mockMvc
            .perform(get(USER_PROFILE_PATH).headers(headers).contextPath(getContextPath()))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    boolean remote =
        JsonPath.read(result.getResponse().getContentAsString(), "$.settings.remoteNotifications");
    assertTrue(remote);

    verifyTokenIntrospectRequest(2);
  }

  @Test
  public void deactivateAccountSuccess() throws Exception {
    HttpHeaders headers = TestUtils.getCommonHeaders(Constants.USER_ID_HEADER);
    headers.set(Constants.USER_ID_HEADER, Constants.USER_ID_VALID);

    StudyReqBean studyReqBean = new StudyReqBean(Constants.STUDY_ID, Constants.DELETE);
    List<StudyReqBean> list = new ArrayList<StudyReqBean>();
    list.add(studyReqBean);
    DeactivateAcctBean acctBean = new DeactivateAcctBean(list);
    String requestJson = getObjectMapper().writeValueAsString(acctBean);

    mockMvc
        .perform(
            delete(DEACTIVATE_PATH)
                .content(requestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(Constants.SUCCESS)));

    verifyTokenIntrospectRequest(1);

    UserDetailsBO daoResp = service.loadUserDetailsByUserId(Constants.USER_ID_VALID);
    assertNull(daoResp);

    verify(
        1, deleteRequestedFor(urlEqualTo("/oauth-scim-service/users/" + Constants.USER_ID_VALID)));
    verify(
        1,
        postRequestedFor(
            urlEqualTo(
                "/mystudies-response-server/participant/withdraw?studyId=studyId1&participantId=4&deleteResponses=delete")));
  }

  @Test
  public void deactivateAccountBadRequest() throws Exception {
    HttpHeaders headers = TestUtils.getCommonHeaders(Constants.USER_ID_HEADER);

    // invalid userId
    headers.set(Constants.USER_ID_HEADER, Constants.INVALID_USER_ID);
    DeactivateAcctBean acctBean = new DeactivateAcctBean();
    String requestJson = getObjectMapper().writeValueAsString(acctBean);
    mockMvc
        .perform(
            delete(DEACTIVATE_PATH)
                .content(requestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest());

    verifyTokenIntrospectRequest(1);
  }

  @Test
  public void resendConfirmationBadRequest() throws Exception {

    HttpHeaders headers =
        TestUtils.getCommonHeaders(Constants.APP_ID_HEADER, Constants.ORG_ID_HEADER);

    // without email
    String requestJson = getLoginBean("");
    mockMvc
        .perform(
            post(RESEND_CONFIRMATION_PATH)
                .content(requestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest());

    // invalid email
    requestJson = getLoginBean(Constants.INVALID_EMAIL);
    mockMvc
        .perform(
            post(RESEND_CONFIRMATION_PATH)
                .content(requestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest());

    // without appId
    headers.set(Constants.APP_ID_HEADER, "");
    requestJson = getLoginBean(Constants.EMAIL_ID);
    mockMvc
        .perform(
            post(RESEND_CONFIRMATION_PATH)
                .content(requestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  public void resendConfirmationSuccess() throws Exception {

    HttpHeaders headers =
        TestUtils.getCommonHeaders(Constants.APP_ID_HEADER, Constants.ORG_ID_HEADER);

    String requestJson = getLoginBean(Constants.VALID_EMAIL);

    mockMvc
        .perform(
            post(RESEND_CONFIRMATION_PATH)
                .content(requestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(Constants.SUCCESS)));

    verify(emailSender, atLeastOnce()).send(isA(MimeMessage.class));
  }

  private String getLoginBean(String emailId) throws JsonProcessingException {
    LoginBean loginBean = new LoginBean(emailId);
    return getObjectMapper().writeValueAsString(loginBean);
  }

  protected ObjectMapper getObjectMapper() {
    return objectMapper;
  }
}
