package net.cyclestreets.api;

public class POICategory
{
  private final String key_;
  private final String shortName_;
  private final String name_;
  
  public POICategory(final String key,
                     final String shortName,
                     final String name,
                     final int count)
  {
    key_ = key;
    shortName_ = shortName;
    name_ = name;
  } // POICategory
  
  public String shortName() { return shortName_; }
  public String name() { return name_; }
} // class POICategory
