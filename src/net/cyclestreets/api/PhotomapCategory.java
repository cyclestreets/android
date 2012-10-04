package net.cyclestreets.api;

public class PhotomapCategory
{
  private String tag_, name_, description_;

  PhotomapCategory(final String tag, 
                   final String name,
                   final String description,
                   final long ordering)
  {
    tag_ = tag;
    name_ = name;
    description_ = description;
  } // PhotomapCategory
  
  public String getName() { return name_; }
  public String getTag() { return tag_; }
  public String getDescription() { return description_; }
  
  @Override
  public String toString() { return tag_; }
} // class PhotomapCategory
