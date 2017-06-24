package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class Segment extends RouteOrSegment {
  @JsonProperty
  private String turn;

  public String getTurn() {
    return turn;
  }
}
