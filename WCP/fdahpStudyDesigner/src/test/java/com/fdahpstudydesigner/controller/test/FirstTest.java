package com.fdahpstudydesigner.controller.test;

import static org.junit.Assert.assertTrue;
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

// fdahpStudyDesigner/src/main/webapp/WEB-INF/ApplicationContext.xml
/*@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "./ApplicationContext.xml")
@WebAppConfiguration(value = "/src/main/webapp/WEB-INF/")*/

/*public class FirstTest {

  @Mock private UsersService usersService;

  @InjectMocks private UsersController userController;

  @Test
  public void test() {
    assertTrue(true);
  }

  @Test
  public void testAddOrEditUserDetails() {

    PowerMockito.mockStatic(FdahpStudyDesignerUtil.class);
    PowerMockito.mockStatic(Integer.class);

    when(FdahpStudyDesignerUtil.isSession(any(HttpServletRequest.class))).thenReturn(true);
    when(FdahpStudyDesignerUtil.isEmpty("userId")).thenReturn(false);
    when(FdahpStudyDesignerUtil.isEmpty("checkRefreshFlag")).thenReturn(false);
    when(Integer.valueOf(anyString())).thenReturn(15);

    //    List<UserBO> userList
    // when(FdahpStudyDesignerUtil.isEmpty("checkRefreshFlag")).thenReturn(false);

    MockHttpServletRequest request = new MockHttpServletRequest();
    userController.getUserList(request);
  }
}*/

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:ApplicationContext.xml"})
@WebAppConfiguration
public class FirstTest {

  @Autowired WebApplicationContext wac;

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void test() {
    assertTrue(true);
  }
}
