/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.controller;

import com.google.cloud.healthcare.fdamystudies.common.BaseMockIT;

public class UserControllerTest extends BaseMockIT {

  /*private static String adminUserRequestJson;

  private static String updateAdminUserRequestJson;

  private UserRegAdminEntity userRegAdminEntity;

  private UserRegAdminEntity adminforUpdate;

  @Autowired private TestDataHelper testDataHelper;

  @Autowired private UserRegAdminRepository userRegAdminRepository;

  @Autowired private StudyPermissionRepository studyPermissionRepository;

  @Autowired private SitePermissionRepository sitePermissionRepository;

  @Autowired private AppPermissionRepository appPermissionRepository;

  private AppEntity appEntity;

  private StudyEntity studyEntity;

  private SiteEntity siteEntity;

  @BeforeEach
  public void setUp() throws JsonParseException, JsonMappingException, IOException {
    userRegAdminEntity = testDataHelper.createUserRegAdmin();
    appEntity = testDataHelper.createAppEntity(userRegAdminEntity);
    studyEntity = testDataHelper.createStudyEntity(userRegAdminEntity, appEntity);
    siteEntity = testDataHelper.createSiteEntity(studyEntity, userRegAdminEntity, appEntity);
    adminUserRequestJson = JsonUtils.readJsonFile(TestConstants.ADMIN_USER_REQUEST_JSON_FILE);
    updateAdminUserRequestJson =
        JsonUtils.readJsonFile(TestConstants.UPDATE_USER_REQUEST_JSON_FILE);
  }

  @Test
  public void shouldReturnPermissionMissingError() throws Exception {
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.set(CommonConstants.USER_ID_HEADER, userRegAdminEntity.getId());
    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_USER.getPath())
                .content(adminUserRequestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error_description").value(ErrorCode.PERMISSION_MISSING.getDescription()));
  }

  @Test
  public void shouldReturnEmailExistsError() throws Exception {
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.set(CommonConstants.USER_ID_HEADER, userRegAdminEntity.getId());
    UserRequest userRequest = newUserRequest();
    userRequest.setEmail(EMAIL_VALUE);
    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_USER.getPath())
                .content(asJsonString(userRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error_description").value(ErrorCode.EMAIL_EXISTS.getDescription()));
  }

  @Test
  public void shouldReturnUserNotFoundError() throws Exception {
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.set(CommonConstants.USER_ID_HEADER, IdGenerator.id());
    UserRequest userRequest = newUserRequest();
    userRequest.setSuperAdmin(false);

    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_USER.getPath())
                .content(asJsonString(userRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.error_description").value(ErrorCode.USER_NOT_FOUND.getDescription()));
  }

  @Test
  public void shouldReturnNotSuperAdminAccessError() throws Exception {
    // Step 1: creating non super admin
    userRegAdminEntity = testDataHelper.createNonSuperAdmin();
    UserRequest userRequest = newUserRequest();
    userRequest.setSuperAdmin(false);

    // Step 2: call the API and expect NOT_SUPER_ADMIN_ACCESS error
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.set(CommonConstants.USER_ID_HEADER, userRegAdminEntity.getId());
    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_USER.getPath())
                .content(asJsonString(userRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(
            jsonPath("$.error_description")
                .value(ErrorCode.NOT_SUPER_ADMIN_ACCESS.getDescription()));
  }

  @Test
  public void shouldReturnBadRequestForFirstNameMissing() throws Exception {
    // Step 1: setting first name as empty
    UserRequest userRequest = newUserRequest();
    userRequest.setFirstName("");

    // Step 2: call the API and expect must not be blank
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.set(CommonConstants.USER_ID_HEADER, userRegAdminEntity.getId());
    mockMvc
        .perform(
            post(ApiEndpoint.ADD_NEW_USER.getPath())
                .content(asJsonString(userRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.violations").isArray())
        .andExpect(jsonPath("$.violations[0].message").value("must not be blank"));
  }

  @Test
  public void shouldCreateSuperAdminUser() throws Exception {
    // Step 1: setting up the request for super admin
    UserRequest userRequest = newUserRequest();

    // Step 2: call the API and expect ADD_NEW_USER_SUCCESS message
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.set(CommonConstants.USER_ID_HEADER, userRegAdminEntity.getId());
    MvcResult result =
        mockMvc
            .perform(
                post(ApiEndpoint.ADD_NEW_USER.getPath())
                    .content(asJsonString(userRequest))
                    .headers(headers)
                    .contextPath(getContextPath()))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value(MessageCode.ADD_NEW_USER_SUCCESS.getMessage()))
            .andExpect(jsonPath("$.userId", notNullValue()))
            .andReturn();

    String userId = JsonPath.read(result.getResponse().getContentAsString(), "$.userId");

    // Step 3: verify saved values
    assertAdminUser(userId, true);
    assertAppPermissionDetails(userId);
    assertStudyPermissionDetails(userId);
    assertSitePermissionDetails(userId);
  }

  @Test
  public void shouldCreateAdminUserForSitePermission() throws Exception {
    // Step 1: setting up the request for site permission
    DocumentContext json = JsonPath.parse(adminUserRequestJson);
    adminUserRequestJson =
        json.set("$.apps[0].studies[0].sites[0].siteId", siteEntity.getId())
            .set("$.apps[0].studies[0].sites[0].selected", true)
            .jsonString();

    // Step 2: call the API and expect ADD_NEW_USER_SUCCESS message
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.set(CommonConstants.USER_ID_HEADER, userRegAdminEntity.getId());
    MvcResult result =
        mockMvc
            .perform(
                post(ApiEndpoint.ADD_NEW_USER.getPath())
                    .content(adminUserRequestJson)
                    .headers(headers)
                    .contextPath(getContextPath()))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value(MessageCode.ADD_NEW_USER_SUCCESS.getMessage()))
            .andExpect(jsonPath("$.userId", notNullValue()))
            .andReturn();

    String userId = JsonPath.read(result.getResponse().getContentAsString(), "$.userId");

    // Step 3: verify saved values
    assertAdminUser(userId, false);
    assertSitePermissionDetails(userId);
  }

  @Test
  public void shouldCreateAdminUserForStudyPermission() throws Exception {
    // Step 1: setting up the request for site permission
    DocumentContext json = JsonPath.parse(adminUserRequestJson);
    adminUserRequestJson =
        json.set("$.apps[0].studies[0].studyId", studyEntity.getId())
            .set("$.apps[0].studies[0].selected", true)
            .jsonString();

    // Step 2: call the API and expect ADD_NEW_USER_SUCCESS message
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.set(CommonConstants.USER_ID_HEADER, userRegAdminEntity.getId());
    MvcResult result =
        mockMvc
            .perform(
                post(ApiEndpoint.ADD_NEW_USER.getPath())
                    .content(adminUserRequestJson)
                    .headers(headers)
                    .contextPath(getContextPath()))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value(MessageCode.ADD_NEW_USER_SUCCESS.getMessage()))
            .andExpect(jsonPath("$.userId", notNullValue()))
            .andReturn();

    String userId = JsonPath.read(result.getResponse().getContentAsString(), "$.userId");

    // Step 3: verify saved values
    assertAdminUser(userId, false);
    assertStudyPermissionDetails(userId);
    assertSitePermissionDetails(userId);
  }

  @Test
  public void shouldCreateAdminUserForAppPermission() throws Exception {
    // Step 1: setting up the request for site permission
    DocumentContext json = JsonPath.parse(adminUserRequestJson);
    adminUserRequestJson =
        json.set("$.apps[0].id", appEntity.getId()).set("$.apps[0].selected", true).jsonString();

    // Step 2: call the API and expect ADD_NEW_USER_SUCCESS message
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    headers.set(CommonConstants.USER_ID_HEADER, userRegAdminEntity.getId());
    MvcResult result =
        mockMvc
            .perform(
                post(ApiEndpoint.ADD_NEW_USER.getPath())
                    .content(adminUserRequestJson)
                    .headers(headers)
                    .contextPath(getContextPath()))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value(MessageCode.ADD_NEW_USER_SUCCESS.getMessage()))
            .andExpect(jsonPath("$.userId", notNullValue()))
            .andReturn();

    String userId = JsonPath.read(result.getResponse().getContentAsString(), "$.userId");

    // Step 3: verify saved values
    assertAdminUser(userId, false);
    assertAppPermissionDetails(userId);
    assertStudyPermissionDetails(userId);
    assertSitePermissionDetails(userId);
  }

  @Test
  public void shouldUpdateSuperAdminUser() throws Exception {
    // Step 1: creating a non super admin
    adminforUpdate = testDataHelper.createNonSuperAdmin();

    // Step 2: call the API and expect UPDATE_USER_SUCCESS message
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    UserRequest userRequest = newUserRequestForUpdate();
    userRequest.setUserId(adminforUpdate.getId());
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER.getPath(), userRegAdminEntity.getId())
                .content(asJsonString(userRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(MessageCode.UPDATE_USER_SUCCESS.getMessage()))
        .andExpect(jsonPath("$.userId", notNullValue()));

    // Step 3: verify updated values
    assertAdminDetails(adminforUpdate.getId(), true);
    assertAppPermissionDetails(adminforUpdate.getId());
    assertStudyPermissionDetails(adminforUpdate.getId());
    assertSitePermissionDetails(adminforUpdate.getId());
  }

  @Test
  public void shouldUpdateAdminUserForSitePermission() throws Exception {
    // Step 1: creating a super admin
    adminforUpdate = testDataHelper.createSuperAdmin();

    // Step 2: call the API and expect UPDATE_USER_SUCCESS message
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    DocumentContext json = JsonPath.parse(updateAdminUserRequestJson);
    updateAdminUserRequestJson =
        json.set("$.apps[0].studies[0].sites[0].siteId", siteEntity.getId())
            .set("$.apps[0].studies[0].sites[0].selected", true)
            .set("$.userId", adminforUpdate.getId())
            .jsonString();
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER.getPath(), userRegAdminEntity.getId())
                .content(updateAdminUserRequestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(MessageCode.UPDATE_USER_SUCCESS.getMessage()))
        .andExpect(jsonPath("$.userId", notNullValue()));

    // Step 3: verify updated values
    assertAdminDetails(adminforUpdate.getId(), false);
    assertSitePermissionDetails(adminforUpdate.getId());
  }

  @Test
  public void shouldUpdateAdminUserForStudyPermission() throws Exception {
    // Step 1: creating a super admin
    adminforUpdate = testDataHelper.createSuperAdmin();

    // Step 2: call the API and expect UPDATE_USER_SUCCESS message
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    DocumentContext json = JsonPath.parse(updateAdminUserRequestJson);
    updateAdminUserRequestJson =
        json.set("$.apps[0].studies[0].studyId", studyEntity.getId())
            .set("$.apps[0].studies[0].selected", true)
            .set("$.userId", adminforUpdate.getId())
            .jsonString();
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER.getPath(), userRegAdminEntity.getId())
                .content(updateAdminUserRequestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(MessageCode.UPDATE_USER_SUCCESS.getMessage()))
        .andExpect(jsonPath("$.userId", notNullValue()));

    // Step 3: verify updated values
    assertAdminDetails(adminforUpdate.getId(), false);
    assertSitePermissionDetails(adminforUpdate.getId());
    assertStudyPermissionDetails(adminforUpdate.getId());
  }

  @Test
  public void shouldUpdateAdminUserForAppPermission() throws Exception {
    // Step 1: creating a super admin
    adminforUpdate = testDataHelper.createSuperAdmin();

    // Step 2: call the API and expect UPDATE_USER_SUCCESS message
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    DocumentContext json = JsonPath.parse(updateAdminUserRequestJson);
    updateAdminUserRequestJson =
        json.set("$.apps[0].id", appEntity.getId())
            .set("$.apps[0].selected", true)
            .set("$.userId", adminforUpdate.getId())
            .jsonString();
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER.getPath(), userRegAdminEntity.getId())
                .content(updateAdminUserRequestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(MessageCode.UPDATE_USER_SUCCESS.getMessage()))
        .andExpect(jsonPath("$.userId", notNullValue()));

    // Step 3: verify updated values
    assertAdminDetails(adminforUpdate.getId(), false);
    assertSitePermissionDetails(adminforUpdate.getId());
    assertStudyPermissionDetails(adminforUpdate.getId());
    assertAppPermissionDetails(adminforUpdate.getId());
  }

  @Test
  public void shouldReturnUserNotFoundErrorForUpdateUser() throws Exception {
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    UserRequest userRequest = newUserRequestForUpdate();
    userRequest.setSuperAdmin(false);
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER.getPath(), userRegAdminEntity.getId())
                .content(asJsonString(userRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.error_description").value(ErrorCode.USER_NOT_FOUND.getDescription()));
  }

  @Test
  public void shouldReturnNotSuperAdminAccessErrorForUpdateUser() throws Exception {
    userRegAdminEntity = testDataHelper.createNonSuperAdmin();
    adminforUpdate = testDataHelper.createSuperAdmin();
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    UserRequest userRequest = newUserRequestForUpdate();
    userRequest.setUserId(adminforUpdate.getId());
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER.getPath(), userRegAdminEntity.getId())
                .content(asJsonString(userRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(
            jsonPath("$.error_description")
                .value(ErrorCode.NOT_SUPER_ADMIN_ACCESS.getDescription()));
  }

  @Test
  public void shouldReturnBadRequestForMissingParameter() throws Exception {
    // Step 1: setting last name as empty
    UserRequest userRequest = newUserRequestForUpdate();
    userRequest.setLastName("");

    // Step 2: call the API and expect must not be blank
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER.getPath(), userRegAdminEntity.getId())
                .content(asJsonString(userRequest))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.violations").isArray())
        .andExpect(jsonPath("$.violations[0].message").value("must not be blank"));
  }

  @Test
  public void shouldReturnPermissionMissingErrorForUpdateUser() throws Exception {
    adminforUpdate = testDataHelper.createSuperAdmin();
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER.getPath(), userRegAdminEntity.getId())
                .content(updateAdminUserRequestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error_description").value(ErrorCode.PERMISSION_MISSING.getDescription()));
  }

  private UserRequest newUserRequestForUpdate() {
    UserRequest userRequest = new UserRequest();
    userRequest.setEmail(NON_SUPER_ADMIN_EMAIL_ID);
    userRequest.setFirstName(TestConstants.UPDATED_FIRST_NAME);
    userRequest.setLastName(TestConstants.UPDATED_LAST_NAME);
    userRequest.setManageLocations(ManageLocation.ALLOW.getValue());
    userRequest.setSuperAdmin(true);
    return userRequest;
  }

  private UserRequest newUserRequest() {
    UserRequest userRequest = new UserRequest();
    userRequest.setEmail(TestConstants.USER_EMAIL_VALUE);
    userRequest.setFirstName(TestConstants.FIRST_NAME);
    userRequest.setLastName(TestConstants.LAST_NAME);
    userRequest.setManageLocations(ManageLocation.ALLOW.getValue());
    userRequest.setSuperAdmin(true);
    return userRequest;
  }

  private void assertSitePermissionDetails(String userId) {
    List<SitePermissionEntity> sitePermissions =
        sitePermissionRepository.findSitePermissionByUserId(userId);
    assertNotNull(sitePermissions);
  }

  private void assertStudyPermissionDetails(String userId) {
    List<StudyPermissionEntity> studyPermissions =
        studyPermissionRepository.findByAdminUser(userId);
    assertNotNull(studyPermissions);
  }

  private void assertAppPermissionDetails(String userId) {
    List<AppPermissionEntity> appPermissions = appPermissionRepository.findByAdminUser(userId);
    assertNotNull(appPermissions);
  }

  private void assertAdminDetails(String userId, boolean isSuperAdmin) {
    Optional<UserRegAdminEntity> optAdminUserEntity = userRegAdminRepository.findById(userId);
    UserRegAdminEntity adminUserEntity = optAdminUserEntity.get();
    assertNotNull(adminUserEntity);
    assertEquals(TestConstants.UPDATED_FIRST_NAME, adminUserEntity.getFirstName());
    assertEquals(TestConstants.UPDATED_LAST_NAME, adminUserEntity.getLastName());
    assertEquals(isSuperAdmin, adminUserEntity.isSuperAdmin());
  }

  private void assertAdminUser(String userId, boolean isSuperAdmin) {
    Optional<UserRegAdminEntity> optAdminUserEntity = userRegAdminRepository.findById(userId);
    UserRegAdminEntity adminUserEntity = optAdminUserEntity.get();
    assertNotNull(adminUserEntity);
    assertEquals(TestConstants.EMAIL_ID, adminUserEntity.getEmail());
    assertEquals(TestConstants.FIRST_NAME, adminUserEntity.getFirstName());
    assertEquals(TestConstants.LAST_NAME, adminUserEntity.getLastName());
    assertEquals(isSuperAdmin, adminUserEntity.isSuperAdmin());
  }

  @AfterEach
  public void cleanUp() throws JsonParseException, JsonMappingException, IOException {
    testDataHelper.getSiteRepository().deleteAll();
    testDataHelper.getStudyRepository().deleteAll();
    testDataHelper.getAppRepository().deleteAll();
    testDataHelper.getUserRegAdminRepository().deleteAll();
  }*/
}
