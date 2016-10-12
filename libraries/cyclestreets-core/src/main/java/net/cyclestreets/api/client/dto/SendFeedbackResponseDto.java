package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.cyclestreets.api.Feedback;
import net.cyclestreets.api.Result;

public class SendFeedbackResponseDto extends ApiResponseDto {
  @JsonProperty
  private String id;

  public Result toFeedbackResult() {
    return wasSuccessful() ? Feedback.ok() : Feedback.error(error);
  }
}
