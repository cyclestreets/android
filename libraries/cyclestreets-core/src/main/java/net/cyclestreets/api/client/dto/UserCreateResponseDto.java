package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.cyclestreets.api.Registration;

public class UserCreateResponseDto extends ApiResponseDto {
  @JsonProperty(value = "successmessage")
  private String successMessage;

  private String getMessage() {
    return wasSuccessful() ? successMessage : error;
  }

  public Registration.Result toRegistrationResult() {
    return new Registration.Result(wasSuccessful(), getMessage());
  }
}
