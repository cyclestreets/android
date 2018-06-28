package net.cyclestreets.util;

import java.util.Iterator;

public class IterableIterator<T> implements Iterable<T>, Iterator<T>
{
  private final Iterator<T> iter_;

  public IterableIterator(final Iterator<T> iter) {
    iter_ = iter;
  }

  @Override
  public boolean hasNext() {
    return iter_.hasNext();
  }

  @Override
  public T next() {
    return iter_.next();
  }

  @Override
  public void remove() {
    iter_.remove();
  }

  @Override
  public Iterator<T> iterator() {
    return iter_;
  }
}
