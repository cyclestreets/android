package net.cyclestreets.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public class Collections
{
  public static <T> List<T> concatenate(final Collection<T> v1, final Collection<T> v2) {
    final List<T> l = new ArrayList<>(v1.size() + v2.size());
    l.addAll(v1);
    l.addAll(v2);
    return l;
  }

  private Collections() { }
}
