package net.cyclestreets.util;

import java.util.Map;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;

public class Collections
{
  static public interface MapBuilder<K,V> extends Map<K, V>
  {
    public MapBuilder<K, V> map(K key, V v);
  } // interface MapBuilder

  static public <K, V> MapBuilder<K,V> map(K key, V value)
  {
    return MapFactory.map(key, value);
  } // map

  static public <T> List<T> list(final T... values)
  {
    return ListFactory.list(values);
  } // list

  static public <T> List<T> list(final Iterator<T> values)
  {
    return ListFactory.list(values);
  } // list

  static public <T> List<T> list(final Collection<T> values)
  {
    return list(values.iterator());
  } // list

  private Collections() { }
} // Collections}
