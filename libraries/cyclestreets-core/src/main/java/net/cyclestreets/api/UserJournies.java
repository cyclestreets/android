package net.cyclestreets.api;

import net.cyclestreets.api.json.JsonReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class UserJournies implements Iterable<UserJourney> {
  private final List<UserJourney> journies_;

  UserJournies() {
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
  public static UserJournies load(final String username) {
    try {
      return ApiClient.getUserJournies(username);
    } catch(Exception e) {
      // let's come back
    } // catch

    return new UserJournies();
  } // load

  ////////////////////////////////////////////////////
  public static Factory<UserJournies> factory() {
    return new UserJourniesFactory();
  } // factory

  private static class UserJourniesFactory implements Factory<UserJournies> {
    private UserJournies journies_;

    UserJourniesFactory() {
      journies_ = new UserJournies();
    } // UserJourniesFactory

    public UserJournies read(final byte[] json) {
      try {
        return doRead(json);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    } // read
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
    public UserJournies doRead(final byte[] json) throws IOException {
      final JsonReader reader = new JsonReader(byteStreamReader(json));
      try {
        reader.beginObject();
        while (reader.hasNext()) {
          reader.skipValue();
        } // while
        reader.endObject();
      }
      finally {
        reader.close();
      }

      return journies_;
    } // doRead

    private Reader byteStreamReader(final byte[] bytes) throws UnsupportedEncodingException {
      final InputStream in = new ByteArrayInputStream(bytes);
      return new InputStreamReader(in, "UTF-8");
    } // byteReader
  } // UserJourniesFactory
} // class UserJournies
