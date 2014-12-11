package net.cyclestreets.api.json;

/**
 * A structure, name or value type in a JSON-encoded string.
 */
public enum JsonToken {
  BEGIN_ARRAY,
  END_ARRAY,
  BEGIN_OBJECT,
  END_OBJECT,
  NAME,
  STRING,
  NUMBER,
  BOOLEAN,
  NULL,
  END_DOCUMENT
}
