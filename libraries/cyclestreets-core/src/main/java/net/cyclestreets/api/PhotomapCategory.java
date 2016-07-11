package net.cyclestreets.api;

public class PhotomapCategory
{
  private String tag, name, description;

  public PhotomapCategory(final String tag,
                          final String name,
                          final String description) {
    this.tag = tag;
    this.name = name;
    this.description = description;
  }
  
  public String getName() { return name; }
  public String getTag() { return tag; }
  public String getDescription() { return description; }
  
  @Override
  public String toString() { return tag; }
}
