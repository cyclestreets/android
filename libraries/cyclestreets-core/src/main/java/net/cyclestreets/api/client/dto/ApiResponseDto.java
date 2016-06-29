package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiResponseDto {
  @JsonProperty(value = "successmessage")
  private String successMessage;

  @JsonProperty
  private String error;

  public boolean wasSuccessful() {
    return (error == null) && (successMessage != null);
  }

  public String getMessage() {
    return wasSuccessful() ? successMessage : error;
  }
}
