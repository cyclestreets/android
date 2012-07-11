package net.cyclestreets.api;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;

public class Blog
{
  private List<BlogEntry> entries_;
  
  private Blog()
  {
    entries_ = new ArrayList<BlogEntry>();
  } // Blog
  
  private void add(final BlogEntry entry)
  {
    if(entries_.size() > 5)
      return;
    entries_.add(entry);
  } // add
  
  public String mostRecent()
  {
    return (entries_.size() != 0) ? entries_.get(0).date() : null;
  } // mostRecent
  
  public String mostRecentTitle()
  {
    return (entries_.size() != 0) ? entries_.get(0).title() : null;
  } // mostRecentTitle
  
  public String toHtml()
  {
    final StringBuilder sb = new StringBuilder();
    for(final BlogEntry be : entries_)
    {
      if(sb.length() != 0)
        sb.append("\n<hr/>\n");
      sb.append(be.toHtml());
    } // for ...
    return sb.toString();
  } // toHtml
  
  ////////////////////////////////////////////////////
  /*
<rss version="2.0"
  xmlns:content="http://purl.org/rss/1.0/modules/content/"
  xmlns:wfw="http://wellformedweb.org/CommentAPI/"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:atom="http://www.w3.org/2005/Atom"
  xmlns:sy="http://purl.org/rss/1.0/modules/syndication/"
  xmlns:slash="http://purl.org/rss/1.0/modules/slash/"
  >

<channel>
  <title>CycleStreets blog</title>
  <atom:link href="http://www.cyclestreets.net/blog/feed/" rel="self" type="application/rss+xml" />
  <link>http://www.cyclestreets.net/blog</link>
  <description>News from CycleStreets</description>
  <lastBuildDate>Wed, 11 Jul 2012 07:00:09 +0000</lastBuildDate>
  <language>en</language>
  <sy:updatePeriod>hourly</sy:updatePeriod>
  <sy:updateFrequency>1</sy:updateFrequency>
  <generator>http://wordpress.org/?v=3.3.2</generator>
    <item>
    <title>Sustrans routes and Google</title>
    <link>http://www.cyclestreets.net/blog/2012/07/11/sustrans-routes-and-google/</link>
    <comments>http://www.cyclestreets.net/blog/2012/07/11/sustrans-routes-and-google/#comments</comments>
    <pubDate>Wed, 11 Jul 2012 07:00:09 +0000</pubDate>
    <dc:creator>Martin</dc:creator>
        <category><![CDATA[Elsewhere]]></category>

    <guid isPermaLink="false">http://www.cyclestreets.net/blog/?p=2478</guid>
    <description><![CDATA[The announcement today that Google are to include Sustrans cycle routes alongside other transport modes is welcome confirmation that cycling is becoming more mainstream. Pitching the various transport options side-by-side will allow people to discover that, for instance, a 3-mile journey across a city can be much quicker to cycle than to drive. Making cycling [...]]]></description>
      <content:encoded><![CDATA[<p>The announcement today that Google are to include Sustrans cycle routes alongside other transport modes is welcome confirmation that cycling is becoming more mainstream. Pitching the various transport options side-by-side will allow people to discover that, for instance, a 3-mile journey across a city can be much quicker to cycle than to drive. Making cycling an equal player helps people discover their local cycle network and help make the case for greater use of bicycles.</p>
<p>It is genuinely heartening to see that, 7 years on from the creation by Cambridge Cycling Campaign of one the first-ever cycle journey planners, that this concept has moved on so much. Indeed, that precursor to CycleStreets that we created used version 1 of the Google Maps API.</p>
<p>Google joining the peleton of websites that show National Cycle Network routes, such as CycleStreets, OpenCycleMap and OpenStreetMap, is another step forward in recognising the existence of the ever-growing cycle network of the UK.</p>
<p><em>2005: The launch of Cambridge Cycling Campaign&#8217;s journey planner, which later become CycleStreets &#8211; how things have moved on! It used the Google Maps framework.</em></p>
<p><em><img class="alignnone  wp-image-2479" title="Cambridge Cycling Campaign journey planner" src="http://www.cyclestreets.net/blog/wp-content/uploads/carta.jpg" alt="" width="476" height="346" /></em></p>
]]></content:encoded>
      <wfw:commentRss>http://www.cyclestreets.net/blog/2012/07/11/sustrans-routes-and-google/feed/</wfw:commentRss>
    <slash:comments>0</slash:comments>
    </item>

   */
  
  static private class BlogFactory extends Factory<Blog>
  {
    private Blog blog_;
    private String title_;
    private String link_;
    private String description_;
    private String date_;
    
    @Override
    protected ContentHandler contentHandler()
    {
      blog_ = new Blog();

      final RootElement root = new RootElement("rss");
      final Element item = root.getChild("channel").getChild("item");
      item.setStartElementListener(new StartElementListener() {
        @Override
        public void start(Attributes attributes)
        {
          title_ = null;
          link_ = null;
          description_ = null;
        }
      });
      item.setEndElementListener(new EndElementListener(){
        public void end() {
          blog_.add(new BlogEntry(title_, link_, description_, date_));
        }
      });
      item.getChild("title").setEndTextElementListener(new EndTextElementListener(){
        public void end(String body) {
          title_ = body;
        }
      });
      item.getChild("link").setEndTextElementListener(new EndTextElementListener(){
        public void end(String body) {
          link_ = body;
        }
      });
      item.getChild("description").setEndTextElementListener(new EndTextElementListener(){
        public void end(String body) {
          description_ = body;
        }
      });
      item.getChild("pubDate").setEndTextElementListener(new EndTextElementListener(){
        public void end(String body) {
          date_ = body;
        }
      });

      return root.getContentHandler();
    } // contentHandler    

    @Override
    protected Blog get()
    {
      return blog_;
    } // get
  } // class BlogFactory

  /////////////////////////////////////////////////////////////////////
  static public Factory<Blog> factory() { 
    return new BlogFactory();
  } // factory
  
  static public Blog load()
  {
    try {
      return ApiClient.getBlogEntries();      
    }
    catch(Exception e) {
      // ah
    }
    return null;
  } // load
} // class Blog
