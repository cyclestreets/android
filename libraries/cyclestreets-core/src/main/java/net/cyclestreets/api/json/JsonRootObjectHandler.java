package net.cyclestreets.api.json;

import java.io.IOException;

public class JsonRootObjectHandler implements JsonRootHandler {
  private JsonObjectHandler objectHandler_;

  public JsonRootObjectHandler() {
    objectHandler_ = new JsonObjectHandler();
  } // JsonRootObject

  @Override
  public JsonObjectHandler getObject(final String name) {
    return objectHandler_.getObject(name);
  } // getChild

  @Override
  public void read(final JsonReader reader) throws IOException {
    objectHandler_.read(null, reader);
  } // read
} // JsonRootObject
