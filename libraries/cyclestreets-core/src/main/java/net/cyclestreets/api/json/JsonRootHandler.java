package net.cyclestreets.api.json;

import java.io.IOException;

public interface JsonRootHandler {
  JsonObjectHandler getObject(final String name);

  void read(final JsonReader reader) throws IOException;
}
