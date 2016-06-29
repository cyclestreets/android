package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ApiResponseDto {
  @JsonProperty
  protected String error;

  protected boolean wasSuccessful() {
    return error == null;
  }
}
