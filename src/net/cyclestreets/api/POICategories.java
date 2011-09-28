package net.cyclestreets.api;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

public class POICategories
{
  private final List<POICategory> cats_;
  
  private POICategories()
  {
    cats_ = new ArrayList<POICategory>();
  } // POICategories
  
  private void add(final POICategory cat) { cats_.add(cat); }
  
  public int count() { return cats_.size(); }
  public POICategory get(int index) { return cats_.get(index); }
  public Iterator<POICategory> iterator() { return cats_.iterator(); }
} // class POICategories
