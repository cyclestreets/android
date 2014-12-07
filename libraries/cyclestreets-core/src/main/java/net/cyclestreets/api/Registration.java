package net.cyclestreets.api;

import org.xml.sax.ContentHandler;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;

public class Registration 
{
  static public class Result
  {
    public Result()
    {
      ok_ = false;
      message_ = "Unknown reason";
    } // RegistrationResult
    
    public Result(final boolean ok, final String message)
    {
      ok_ = ok;
      message_ = message;
    } // Result
    
    public boolean ok() { return ok_; }
    
    public String message() 
    {
      if(ok())
        return "Your account has been registered.\n\nAn email has been sent to the address you gave.\n\nWhen the email arrives, follow the instructions it contains to complete the registration.";
      return "Your account could not be registered.\n\n" + message_; 
    } // message  
    
    boolean ok_;
    String message_;
  } // class Result  
  
  //////////////////////////////////////////////////////
  static public Result register(final String username, 
                                final String password,
                                final String name,
                                final String email) 
  {
    try {
      return ApiClient.register(username, password, name, email);
    } // try
    catch(Exception e) {
      return new Result(false, e.getMessage());
    } // catch
  } // register
  
  ///////////////////////////////////////////////////
  /*
  <?xml version="1.0"?>
  <signin>
    <request></request>
    <result>
      <code>0</code>
      <message>No username was supplied.</message>
    </result>
  </signin>
  */

  static public Factory<Result> factory() { 
    return new RegistrationFactory();
  } // factory

  static private class RegistrationFactory extends Factory.XmlReader<Result>
  {    
    private Registration.Result result_;
    
    @Override
    protected ContentHandler contentHandler()
    {
      result_ = new Result();
      
      final RootElement root = new RootElement("signin");
      final Element code = root.getChild("result").getChild("code");
      final Element message = root.getChild("message").getChild("message");

      code.setEndTextElementListener(new EndTextElementListener() {
        public void end(String body) { result_.ok_ = "1".equals(body); }
      });
      message.setEndTextElementListener(new EndTextElementListener() {
        public void end(String body) { result_.message_ = body; }
      });
      
      return root.getContentHandler();
    } // contentHandler

    @Override
    protected Registration.Result get()
    {
      return result_;
    } // get
  } // class RegistrationFactory
} // class Registration
