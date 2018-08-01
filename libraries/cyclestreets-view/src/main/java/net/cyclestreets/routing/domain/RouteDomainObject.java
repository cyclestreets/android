package net.cyclestreets.routing.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class RouteDomainObject {
  @JsonProperty
  public String name;
  @JsonProperty
  public int grammesCO2saved;
  @JsonProperty
  public int calories;
  @JsonProperty
  public String plan;
  @JsonProperty
  public int speed;
  @JsonProperty
  public int itinerary;
  //TODO: some confusion over usage of name/start
  @JsonProperty
  public String start;
  @JsonProperty
  public String finish;

  @Override
  public String toString() {
    String sb = "RouteDomainObject{" + "name='" + name + '\'' +
        ", grammesCO2saved=" + grammesCO2saved +
        ", calories=" + calories +
        ", plan='" + plan + '\'' +
        ", speed=" + speed +
        ", itinerary=" + itinerary +
        ", start='" + start + '\'' +
        ", finish='" + finish + '\'' +
        '}';
    return sb;
  }
}
