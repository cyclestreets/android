package net.cyclestreets.api;

import java.util.Map;

public interface ApiCustomiser {
  <T> void customise(final String path,
                     final Map<String, T> params);
}
