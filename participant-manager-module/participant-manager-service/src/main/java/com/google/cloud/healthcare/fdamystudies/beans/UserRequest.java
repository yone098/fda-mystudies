package com.google.cloud.healthcare.fdamystudies.beans;

import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Component
@Scope(value = "prototype")
public class UserRequest {

  @ToString.Exclude
  @NotBlank
  @Size(max = 320)
  @Email
  private String email;

  @NotBlank
  @Size(max = 200)
  @ToString.Exclude
  private String firstName;

  @NotBlank
  @Size(max = 200)
  @ToString.Exclude
  private String lastName;

  @NotNull
  @Min(0)
  @Max(2)
  private Integer manageLocations;

  @NotNull private boolean superAdmin;

  private List<UserAppPermissionRequest> apps;

  private String userId;

  private String superAdminUserId;
}
