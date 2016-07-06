package net.cyclestreets.api.client.dto;

import net.cyclestreets.api.Blog;
import net.cyclestreets.api.BlogEntry;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "rss")
public class BlogFeedDto {
  @Element
  private BlogChannelDto channel;

  public static class BlogChannelDto {
    @ElementList(inline = true)
    private List<BlogItemDto> items;
  }

  @Root(name = "item")
  public static class BlogItemDto {
    @Element
    private String title;
    @Element
    private String link;
    @Element
    private String description;
    @Element
    private String pubDate;

    private BlogEntry toBlogEntry() {
      return new BlogEntry(title, link, description, pubDate);
    }
  }

  public Blog toBlog() {
    List<BlogEntry> entries = new ArrayList<>();
    for (BlogItemDto item : channel.items) {
      entries.add(item.toBlogEntry());
    }
    return new Blog(entries);
  }
}
