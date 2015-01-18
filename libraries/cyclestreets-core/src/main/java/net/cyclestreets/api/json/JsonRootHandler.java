package net.cyclestreets.api.json;

import java.io.IOException;

public interface JsonRootHandler {
  JsonArrayHandler getArray(final String name);
  JsonObjectHandler getObject(final String name);

  void read(final JsonReader reader) throws IOException;
}
