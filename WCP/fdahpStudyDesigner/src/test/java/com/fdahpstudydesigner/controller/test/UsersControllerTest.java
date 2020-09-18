package com.fdahpstudydesigner.controller.test;

import javax.servlet.ServletContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:TestApplicationContext.xml"})
@WebAppConfiguration
public class UsersControllerTest {

  @Autowired private WebApplicationContext webApplicationContext;

  protected MockMvc mockMvc;

  @Before
  public void setUp() throws Exception {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
  }

  @Test
  public void viewUserDetailsSuccess() throws Exception {
    ServletContext servletContxt = webApplicationContext.getServletContext();
    Assert.assertNotNull(servletContxt);
  }
}
