package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.cyclestreets.api.Feedback;

public class SendFeedbackResponseDto extends ApiResponseDto {
  @JsonProperty
  private String id;

  public Feedback.Result toFeedbackResult() {
    return wasSuccessful() ? new Feedback.Result() : new Feedback.Result(error);
  }
}
