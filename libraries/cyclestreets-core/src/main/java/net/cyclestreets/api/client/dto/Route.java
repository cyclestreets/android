package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class Route extends RouteOrSegment {
  @JsonProperty
  private String start;
  @JsonProperty
  private String finish;

  public String getStart() {
    return start;
  }

  public String getFinish() {
    return finish;
  }
}
