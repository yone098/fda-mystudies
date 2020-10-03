package com.hphc.mystudies.controller;

import static org.junit.Assert.assertEquals;

import com.hphc.mystudies.config.BaseMockIT;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.Test;

public class HelloWorldControllerTest extends BaseMockIT {

  @Test
  public void helloWorld() {
    Response response = target("/hello").request().get();

    assertEquals("Http Response should be 200: ", Status.OK.getStatusCode(), response.getStatus());
    assertEquals(
        "Http Content-Type should be: ",
        MediaType.TEXT_HTML,
        response.getHeaders().getHeader(HttpHeaders.CONTENT_TYPE));

    String content = response.readEntity(String.class);
    assertEquals("Content of ressponse is: ", "Hello World!", content);
  }
}
