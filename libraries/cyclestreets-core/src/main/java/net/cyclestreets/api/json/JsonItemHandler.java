package net.cyclestreets.api.json;

import java.io.IOException;

public interface JsonItemHandler {
  void read(final String name, final JsonReader reader) throws IOException;
}
