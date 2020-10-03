package com.hphc.mystudies.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class HelloWorldController {

  @GET
  @Path("/hello")
  public String sayHello() {
    return "Hello World!";
  }
}
