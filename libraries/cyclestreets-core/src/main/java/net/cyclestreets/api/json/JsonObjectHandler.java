package net.cyclestreets.api.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonObjectHandler implements JsonItemHandler {
  public interface BeginListener {
    void begin(String name);
  }
  public interface EndListener {
    void end();
  }
  public interface Listener extends BeginListener, EndListener {
  }

  public static String ANY_OBJECT = null;

  private Map<String, JsonItemHandler> items_;
  private JsonItemHandler anyItem_;
  private BeginListener begin_;
  private EndListener end_;

  public JsonObjectHandler() {
    items_ = new HashMap<>();
    anyItem_ = null;
    begin_ = null;
    end_ = null;
  } // JsonRootObject

  public void setBeginObjectListener(final BeginListener begin) { begin_ = begin; }
  public void setEndObjectListener(final EndListener end) { end_ = end; }
  public void setObjectListener(final Listener obj) {
    begin_ = obj;
    end_ = obj;
  } // setObjectListener

  public final JsonObjectHandler getObject(final String name) {
    return (JsonObjectHandler)getChild(name, new JsonObjectHandler());
  } // getObject
  public final JsonStringHandler getString(final String name) {
    return (JsonStringHandler)getChild(name, new JsonStringHandler());
  } // getString

  private JsonItemHandler getChild(final String name, final JsonItemHandler newHandler) {
    if (name == ANY_OBJECT) {
      if (anyItem_ == null)
        anyItem_ = newHandler;
      return anyItem_;
    } // if ...

    if (!items_.containsKey(name))
      items_.put(name, newHandler);
    return items_.get(name);
  } // getChild

  @Override
  public final void read(final String objectName, final JsonReader reader) throws IOException {
    if (begin_ != null)
      begin_.begin(objectName);

    reader.beginObject();

    while(reader.hasNext()) {
      final String name = reader.nextName();

      final JsonItemHandler item = items_.get(name);
      if (item != null)
        item.read(name, reader);
      else if (anyItem_ != null)
        anyItem_.read(name, reader);
      else
        reader.skipValue();
    } // while ...

    reader.endObject();

    if (end_ != null)
      end_.end();
  } // read
} // JsonObject
