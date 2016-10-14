package net.cyclestreets.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collection;

import net.cyclestreets.util.Collections.MapBuilder;

public class MapFactory<K, V> 
{
  static public <K, V> MapBuilder<K, V> map(K key, V value)
  {
	final Builder<K, V> builder = new Builder<>();
	return builder.map(key, value);
  } // map

  static public class Builder<K, V> implements MapBuilder<K, V>
  {
	private final Map<K, V> backing_;
		
	private Builder() 
	{
	  backing_ = new HashMap<>();
	} // Builder

	public MapBuilder<K, V> map(K key, V value)
	{
	  backing_.put(key, value);
	  return this;
	} // map

	public void clear() { backing_.clear(); }
	public boolean containsKey(Object key) { return backing_.containsKey(key); }
	public boolean containsValue(Object value) { return backing_.containsKey(value); }
	public Set<Map.Entry<K, V>> entrySet() { return backing_.entrySet(); }
	public boolean equals(Object o) { return backing_.equals(o); } 
	public V get(Object key) { return backing_.get(key); }
	public int hashCode() { return backing_.hashCode(); }
	public boolean isEmpty() { return backing_.isEmpty(); }
	public Set<K> keySet() { return backing_.keySet(); }
	public V put(K key, V value) { return backing_.put(key, value); }
	public void putAll(Map<? extends K, ? extends V> m) { backing_.putAll(m); }
	public V remove(Object key) { return backing_.remove(key); }
	public int size() { return backing_.size(); }
	public Collection<V> values() { return backing_.values(); }
  } // class Builder
} // MapFactory
