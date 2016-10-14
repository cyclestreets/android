package net.cyclestreets.api.json;


import java.io.IOException;

public class JsonStringHandler implements JsonItemHandler {
  public interface Listener {
    void string(final String name, final String value);
  } // Listener

  private Listener listener_;

  public void setListener(final Listener listener) { listener_ = listener; }

  public final void read(final String name, final JsonReader reader) throws IOException {
    String value = null;

    if (reader.peek() != JsonToken.NULL)
      value = reader.nextString();
    else
      reader.nextNull();

    if (listener_ != null)
      listener_.string(name, value);
  } // read
} // JsonStringHandler
