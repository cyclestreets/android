package net.cyclestreets.api;

import net.cyclestreets.api.json.JsonObjectHandler;
import net.cyclestreets.api.json.JsonRootHandler;
import net.cyclestreets.api.json.JsonRootObjectHandler;
import net.cyclestreets.api.json.JsonStringHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class UserJourneys implements Iterable<UserJourney> {
  private final List<UserJourney> journies_;

  UserJourneys() {
    journies_ = new ArrayList<>();
  } // UserJournies

  @Override
  public Iterator<UserJourney> iterator() { return journies_.iterator(); }

  public int size() { return journies_.size(); }
  public UserJourney get(int index) { return journies_.get(index); }

  private void add(final UserJourney journey) {
    journies_.add(journey);
  } // add

  ///////////////////////////////////////////////////
  public static UserJourneys load(final String username) {
    try {
      return ApiClient.getUserJournies(username);
    } catch(Exception e) {
      // let's come back
    } // catch

    return new UserJourneys();
  } // load

  ////////////////////////////////////////////////////
  public static Factory<UserJourneys> factory() {
    return new UserJourneysFactory();
  } // factory

  private static class UserJourneysFactory extends Factory.JsonProcessor<UserJourneys> {
    private UserJourneys journeys_;
    private String title_;
    private int id_;

    UserJourneysFactory() {
      journeys_ = new UserJourneys();
    } // UserJourneysFactory

/*
{
    "pagination": {
        "latest": 43106958,
        "urlLater": "https://api.cyclestreets.net/v2/journeys.user?username=martin&format=flat&limit=3&datetime=friendly&after=43106958",
        "hasLater": false,
        "top": 43106958,
        "bottom": 43089395,
        "hasEarlier": true,
        "urlEarlier": "https://api.cyclestreets.net/v2/journeys.user?username=martin&format=flat&limit=3&datetime=friendly&before=43089395",
        "earliest": 0,
        "count": 3,
        "total": 889
    },
    "journeys": {
        "43106958": {
            "id": "43106958",
            "name": "Covent Garden Piazza to Station Approach",
            "plans": {
                "balanced": {
                    "name": "Balanced",
                    "url": "https://api.cyclestreets.net/v2/journey.retrieve?itinerary=43106958&plan=balanced"
                },
                "quietest": {
                    "name": "Quietest",
                    "url": "https://api.cyclestreets.net/v2/journey.retrieve?itinerary=43106958&plan=quietest"
                },
                "fastest": {
                    "name": "Fastest",
                    "url": "https://api.cyclestreets.net/v2/journey.retrieve?itinerary=43106958&plan=fastest"
                }
            },
            "datetime": "2:46pm, 20th December 2014",
            "idFormatted": "43,106,958",
            "url": "http://www.cyclestreets.net/journey/43106958/"
        },
        "43106946": {
            "id": "43106946",
            "name": "Suffolk Place to York Road",
            "plans": {
                "balanced": {
                    "name": "Balanced",
                    "url": "https://api.cyclestreets.net/v2/journey.retrieve?itinerary=43106946&plan=balanced"
                },
                "quietest": {
                    "name": "Quietest",
                    "url": "https://api.cyclestreets.net/v2/journey.retrieve?itinerary=43106946&plan=quietest"
                },
                "fastest": {
                    "name": "Fastest",
                    "url": "https://api.cyclestreets.net/v2/journey.retrieve?itinerary=43106946&plan=fastest"
                }
            },
            "datetime": "2:42pm, 20th December 2014",
            "idFormatted": "43,106,946",
            "url": "http://www.cyclestreets.net/journey/43106946/"
        },
        "43089395": {
            "id": "43089395",
            "name": "Hedingham Close to Old Montague Street",
            "plans": {
                "balanced": {
                    "name": "Balanced",
                    "url": "https://api.cyclestreets.net/v2/journey.retrieve?itinerary=43089395&plan=balanced"
                },
                "quietest": {
                    "name": "Quietest",
                    "url": "https://api.cyclestreets.net/v2/journey.retrieve?itinerary=43089395&plan=quietest"
                },
                "fastest": {
                    "name": "Fastest",
                    "url": "https://api.cyclestreets.net/v2/journey.retrieve?itinerary=43089395&plan=fastest"
                }
            },
            "datetime": "4:08pm, 16th December 2014",
            "idFormatted": "43,089,395",
            "url": "http://www.cyclestreets.net/journey/43089395/"
        }
    }
}
 */
    protected UserJourneys get() { return journeys_; }

    @Override
    protected JsonRootHandler rootHandler() {
      final JsonRootObjectHandler rootHandler = new JsonRootObjectHandler();

      final JsonObjectHandler journey = rootHandler.getObject("journeys").getObject(JsonObjectHandler.ANY_OBJECT);
      journey.getString("name").setListener(new JsonStringHandler.Listener() {
        @Override
        public void string(String name, String value) {
          title_ = value;
        }
      });
      journey.getString("id").setListener(new JsonStringHandler.Listener() {
        @Override
        public void string(String name, String value) {
          id_ = Integer.parseInt(value);
        }
      });
      journey.setEndObjectListener(new JsonObjectHandler.EndListener() {
        @Override
        public void end() {
          journeys_.add(new UserJourney(title_, id_));
        }
      });

      return rootHandler;
    } // rootHandler

  } // UserJourneysFactory
} // class UserJourneys
