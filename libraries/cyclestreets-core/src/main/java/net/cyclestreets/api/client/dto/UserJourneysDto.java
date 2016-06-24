package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.cyclestreets.api.UserJourney;
import net.cyclestreets.api.UserJourneys;

import java.util.HashMap;
import java.util.Map;

public final class UserJourneysDto {

  @JsonProperty
  private final Map<String, UserJourney> journeys = new HashMap<>();

  public UserJourneys toUserJourneys() {
    return new UserJourneys(journeys.values());
  }
}
