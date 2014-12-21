package net.cyclestreets.api;

import net.cyclestreets.api.json.JsonReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public final class UserJournies {
  private final List<UserJourney> journies_;

  UserJournies() {
    journies_ = new ArrayList<>();
  } // UserJournies

  private void add(final UserJourney journey) {
    journies_.add(journey);
  } // add

  ///////////////////////////////////////////////////
  public UserJournies load(final String username) throws Exception {
    return ApiClient.getUserJournies(username);
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
