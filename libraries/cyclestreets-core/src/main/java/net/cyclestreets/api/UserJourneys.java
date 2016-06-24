package net.cyclestreets.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class UserJourneys implements Iterable<UserJourney> {
  private final List<UserJourney> journeys = new ArrayList<>();

  private UserJourneys() {}

  public UserJourneys(Collection<UserJourney> userJourneys) {
    journeys.addAll(userJourneys);
  }

  @Override
  public Iterator<UserJourney> iterator() {
    return journeys.iterator();
  }

  public int size() {
    return journeys.size();
  }

  public UserJourney get(int index) {
    return journeys.get(index);
  }

  public static UserJourneys load(final String username) {
    try {
      return ApiClient.getUserJournies(username);
    } catch (Exception e) {
      // let's come back
    }
    return new UserJourneys();
  }
}
