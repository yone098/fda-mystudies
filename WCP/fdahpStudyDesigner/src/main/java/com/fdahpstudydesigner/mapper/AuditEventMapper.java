package com.fdahpstudydesigner.mapper;

import com.fdahpstudydesigner.bean.AuditLogEventRequest;
import com.fdahpstudydesigner.common.MobilePlatform;
import com.fdahpstudydesigner.common.StudyBuilderAuditEvent;
import com.fdahpstudydesigner.common.UserAccessLevel;
import com.fdahpstudydesigner.util.FdahpStudyDesignerUtil;
// import com.google.cloud.healthcare.fdamystudies.common.CommonApplicationPropertyConfig;
// import com.fdahpstudydesigner.common.MobilePlatform;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
// TODO:[Aswini] remove CommonApplicationPropertyConfig.java file dependency
public final class AuditEventMapper {

  private AuditEventMapper() {}

  private static final String APP_ID = "appId";

  private static final String MOBILE_PLATFORM = "mobilePlatform";

  private static final String CORRELATION_ID = "correlationId";

  private static final String USER_ID = "userId";

  private static final String APP_VERSION = "appVersion";

  private static final String SOURCE = "source";

  public static AuditLogEventRequest fromHttpServletRequest(HttpServletRequest request) {
    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    auditRequest.setAppId(getValue(request, APP_ID));
    auditRequest.setAppVersion(getValue(request, APP_VERSION));
    auditRequest.setUserId(getValue(request, USER_ID));
    auditRequest.setUserIp(getUserIP(request));

    MobilePlatform mobilePlatform = MobilePlatform.fromValue(getValue(request, MOBILE_PLATFORM));
    auditRequest.setMobilePlatform(mobilePlatform.getValue());
    return auditRequest;
  }

  private static String getValue(HttpServletRequest request, String name) {
    String value = request.getHeader(name);
    if (StringUtils.isEmpty(value)) {
      value = getCookieValue(request, name);
    }
    return value;
  }

  private static String getUserIP(HttpServletRequest request) {
    return StringUtils.defaultIfEmpty(
        request.getHeader("X-FORWARDED-FOR"), request.getRemoteAddr());
  }

  private static String getCookieValue(HttpServletRequest req, String cookieName) {
    if (req != null && req.getCookies() != null) {
      for (Cookie cookie : req.getCookies()) {
        if (cookie.getName().equals(cookieName)) {
          return cookie.getValue();
        } else {
          return null;
        }
      }
    }
    return null;
  }

  public static AuditLogEventRequest fromAuditLogEventEnumAndCommonPropConfig(
      StudyBuilderAuditEvent eventEnum, AuditLogEventRequest auditRequest) {
    Map<String, String> map = FdahpStudyDesignerUtil.getAppProperties();
    String applicationVersion = map.get("applicationVersion");

    auditRequest.setEventCode(eventEnum.getEventCode());
    auditRequest.setSource(eventEnum.getSource().getValue());
    auditRequest.setDestination(eventEnum.getDestination().getValue());
    /*
    if (eventEnum.getResourceServer().getValue().isEmpty()) {
      auditRequest.setResourceServer(eventEnum.getResourceServer().getValue());
    }
    */
    auditRequest.setUserAccessLevel(UserAccessLevel.STUDY_BUILDER_ADMIN.getValue());
    auditRequest.setSourceApplicationVersion(applicationVersion);
    auditRequest.setDestinationApplicationVersion(applicationVersion);
    auditRequest.setPlatformVersion(applicationVersion);
    auditRequest.setOccured(new Timestamp(Instant.now().toEpochMilli()));
    return auditRequest;
  }
}
