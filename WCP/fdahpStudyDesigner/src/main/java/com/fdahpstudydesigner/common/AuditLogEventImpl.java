package com.fdahpstudydesigner.common;

public abstract class AuditLogEventImpl implements AuditLogEvent {
  PlatformComponent getSource() {
    return null;
  }

  PlatformComponent getDestination() {
    return null;
  }

  PlatformComponent getResourceServer() {
    return null;
  }

  String getEventName() {
    return null;
  }

  String getDescription() {
    return null;
  }

  UserAccessLevel getUserAccessLevel() {
    return null;
  }

  String getEventCode() {
    return null;
  }
}
