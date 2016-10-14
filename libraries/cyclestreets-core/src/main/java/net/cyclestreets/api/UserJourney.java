package net.cyclestreets.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class UserJourney {

  @JsonProperty
  private String name;

  @JsonProperty
  private int id;

  public String name() {
    return name;
  }

  public int id() {
    return id;
  }
}
