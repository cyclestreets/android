package net.cyclestreets.api;

public class BlogEntry
{
  private final String title;
  private final String link;
  private final String description;
  private final String date;
  
  public BlogEntry(final String title,
                   final String link,
                   final String description,
                   final String date) {
    this.title = title;
    this.link = link;
    this.description = description;
    this.date = date;
  }
  
  public String title() { return title; }
  public String date() { return date; }
  
  public String toHtml() {
    return String.format("<h3><a href='%s'>%s</a></h3><p>%s</p><p><small>%s</small></p>",
            link, title, description, date);
  }
}
