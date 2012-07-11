package net.cyclestreets.api;

public class BlogEntry
{
  private final String title_;
  private final String link_;
  private final String description_;
  private final String date_;
  
  public BlogEntry(final String title,
                   final String link,
                   final String description,
                   final String date)
  {
    title_ = title;
    link_ = link;
    description_ = description;
    date_ = date;
  } // BlogEntry
  
  public String title() { return title_; }
  public String date() { return date_; }
  
  public String toHtml()
  {
    return String.format("<h3><a href='%s'>%s</a><h3><p>%s</p><p><small>%s</small></p>",
                         link_, title_, description_, date_); 
  } // toHtml
} // class BlogEntry
