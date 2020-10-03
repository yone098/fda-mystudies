package com.hphc.mystudies.config;

import com.hphc.mystudies.controller.HelloWorldController;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

public class BaseMockIT extends JerseyTest {

  @Override
  protected Application configure() {
    return new ResourceConfig(HelloWorldController.class);
  }
}
