package net.cyclestreets.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class ListFactory<T>
{
  static public <T> List<T> list(final T... values)
  {
    final List<T> l = new ArrayList<>();
    Collections.addAll(l, values);

    return l;
  } // list

  static public <T> List<T> list(final Iterator<T> values)
  {
    final List<T> l = new ArrayList<>();
    
    while(values.hasNext())
      l.add(values.next());

    return l;
  } // list
} // ListFactory