package com.google.cloud.healthcare.fdamystudies.oauthscim.controller;

import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.ACCOUNT_STATUS_COOKIE;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.ENV_COOKIE;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.ERROR_VIEW_NAME;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.MOBILE_PLATFORM_COOKIE;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.USER_ID_COOKIE;

import com.google.cloud.healthcare.fdamystudies.common.UserAccountStatus;
import com.google.cloud.healthcare.fdamystudies.oauthscim.config.RedirectConfig;
import com.google.cloud.healthcare.fdamystudies.oauthscim.model.UserEntity;
import com.google.cloud.healthcare.fdamystudies.oauthscim.service.UserService;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.WebUtils;

@Controller
public class CallbackController {

  private XLogger logger = XLoggerFactory.getXLogger(CallbackController.class.getName());

  @Autowired private RedirectConfig redirectConfig;

  @Autowired private UserService userService;

  @GetMapping(value = "/callback")
  public String login(
      @RequestParam String code,
      @CookieValue(name = ACCOUNT_STATUS_COOKIE) String accountStatus,
      HttpServletRequest request,
      HttpServletResponse response,
      Model model) {
    logger.entry(String.format("%s request", request.getRequestURI()));

    if (StringUtils.isEmpty(code)) {
      logger.error("auth code is empty, return error view");
      return ERROR_VIEW_NAME;
    }

    String userId = WebUtils.getCookie(request, USER_ID_COOKIE).getValue();
    String mobilePlatform = WebUtils.getCookie(request, MOBILE_PLATFORM_COOKIE).getValue();
    String callbackUrl = redirectConfig.getCallbackUrl(mobilePlatform);
    Cookie envCookie = WebUtils.getCookie(request, ENV_COOKIE);
    if (envCookie != null) {
      String env = envCookie.getValue();
      logger.debug("env=" + env);
      if (StringUtils.equalsIgnoreCase(env, "localhost")) {
        callbackUrl = "http://localhost:4200/#/callback";
      }
    } else {
      logger.debug("env is null");
    }

    String email = null;
    if (StringUtils.equals(
        accountStatus, String.valueOf(UserAccountStatus.PASSWORD_RESET.getStatus()))) {
      Optional<UserEntity> optUserEntity = userService.findByUserId(userId);
      if (optUserEntity.isPresent()) {
        UserEntity user = optUserEntity.get();
        email = user.getEmail();
      }
    }

    String redirectUrl =
        String.format(
            "%s?code=%s&userId=%s&accountStatus=%s&email=%s",
            callbackUrl, code, userId, accountStatus, email);

    logger.debug("callback redirect url=" + redirectUrl);

    logger.exit(String.format("redirect to %s from /login", callbackUrl));
    return redirect(response, redirectUrl);
  }

  private String redirect(HttpServletResponse response, String redirectUrl) {
    response.setHeader("Location", redirectUrl);
    response.setStatus(HttpStatus.FOUND.value());
    return "redirect:" + redirectUrl;
  }
}
