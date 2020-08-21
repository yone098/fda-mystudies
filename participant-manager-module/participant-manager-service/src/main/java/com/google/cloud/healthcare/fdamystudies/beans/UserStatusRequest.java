package com.google.cloud.healthcare.fdamystudies.beans;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Component
@Scope(value = "prototype")
public class UserStatusRequest {

  @Min(0)
  @Max(2)
  @NotNull
  private Integer status;
}
