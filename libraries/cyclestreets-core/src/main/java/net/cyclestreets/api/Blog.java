package net.cyclestreets.api;

import java.util.ArrayList;
import java.util.List;

public class Blog
{
  private static final Blog NULL_BLOG;
  static {
    NULL_BLOG = new Blog(new ArrayList<BlogEntry>() {{
      add(new BlogEntry("ERROR", "http://www.cyclestreets.net/blog/", "Could not retrieve CycleStreets blog entries", ""));
    }});
  }
  private List<BlogEntry> entries = new ArrayList<>();
  
  public Blog(List<BlogEntry> entries) {
    // Add up to the 5 most recent blog entries
    this.entries.addAll(entries.subList(0, Math.min(5, entries.size())));
  }
  
  public boolean isNull() {
    return this == NULL_BLOG;
  }
  
  public String mostRecent() {
    return (entries.size() != 0) ? entries.get(0).date() : null;
  }
  
  public String mostRecentTitle() {
    return (entries.size() != 0) ? entries.get(0).title() : null;
  }
  
  public String toHtml() {
    final StringBuilder sb = new StringBuilder();
    for (final BlogEntry be : entries) {
      if (sb.length() != 0)
        sb.append("\n<hr/>\n");
      sb.append(be.toHtml());
    }
    return sb.toString();
  }

  public static Blog load() {
    try {
      return ApiClient.getBlogEntries();      
    }
    catch(Exception e) {
      // ah
    }
    return NULL_BLOG;
  }
}
