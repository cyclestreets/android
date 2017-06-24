package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class Waypoint {

  @JsonProperty
  private int sequenceId;
  @JsonProperty
  private double longitude;
  @JsonProperty
  private double latitude;

  public int getSequenceId() {
    return sequenceId;
  }

  public double getLongitude() {
    return longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  @Override
  public String toString() {
    return "Waypoint{" +
            "sequenceId=" + sequenceId +
            ", longitude=" + longitude +
            ", latitude=" + latitude +
            '}';
  }

  // Workaround for weird JSON wrapping of objects.
  public static class WaypointWrapper {
    @JsonProperty(value = "@attributes")
    private Waypoint waypoint;

    public Waypoint getContents() {
      return waypoint;
    }
  }
}
