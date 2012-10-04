package net.cyclestreets.api;

import org.xml.sax.ContentHandler;
import android.sax.RootElement;
import android.sax.EndTextElementListener;
import android.sax.Element;

public class Feedback
{
  static public class Result
  {
    private Result() { }
    
    public boolean ok() { return "1".equals(code_); }
    public String message() { return message_; }
    
    private String code_; 
    private String message_;
  } // Result

  static public Feedback.Result send(final int itinerary, 
                                     final String comments,
                                     final String name,
                                     final String email)
    throws Exception
  {
    return ApiClient.sendFeedback(itinerary, comments, name, email);
  } // send
  
  /* <feedback>
   *    <request>713240</request>
   *   <result>
   *      <code>1</code>
   *     <message>Thank you for submitting this feedback. We will 
   *      get back to you when we have checked this out.</message>
   *     <url>http://www.cyclestreets.net/journey/713240/</url>
   *     <feedbackid>4267</feedbackid>
   *   </result>
   *   </feedback>
   */
  static public Factory<Result> factory() { 
    return new FeedbackFactory();
  } // factory

  static private class FeedbackFactory extends Factory<Feedback.Result>
  {    
    private Feedback.Result result_;
    
    @Override
    protected ContentHandler contentHandler()
    {
      result_ = new Result();
      
      final RootElement root = new RootElement("feedback");
      final Element code = root.getChild("result").getChild("code");
      final Element message = root.getChild("result").getChild("message");

      code.setEndTextElementListener(new EndTextElementListener() {
        public void end(String body) { result_.code_ = body; }
      });
      message.setEndTextElementListener(new EndTextElementListener() {
        public void end(String body) { result_.message_ = body; }
      });
      
      return root.getContentHandler();
    } // contentHandler

    @Override
    protected Feedback.Result get()
    {
      return result_;
    } // get
  } // class FeedbackFactory
} // class Feedback
