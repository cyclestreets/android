package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.cyclestreets.api.Registration;
import net.cyclestreets.api.Result;

public class UserCreateResponseDto extends ApiResponseDto {
  @JsonProperty(value = "successmessage")
  private String successMessage;

  public Result toRegistrationResult() {
    return wasSuccessful() ? Registration.ok(): Registration.error(error);
  }
}
