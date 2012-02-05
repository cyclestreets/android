package net.cyclestreets.api;

import org.xml.sax.ContentHandler;

import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.Element;

public class Signin 
{
  static public class Result 
  {
  	private Result() { }
  	private Result(final String errorMessage)
  	{
  		error_ = errorMessage;
  	} // SigninResult
  	
  	public boolean ok() 
  	{ 
  		return validated_ != null &&
  			     validated_.length() != 0; 
  	} // ok
  	
  	public String email() { return email_; }
  	public String name() { return name_; }
  	
  	public String error() 
  	{
  		if(error_ != null)
  			return "Error : " + error_;
  		return "Unknown error";
  	} // error
  	
  	private String validated_;
  	private String email_;
  	private String name_;
  	private String error_;
  } // class Result
  
  static public Result signin(final String username, 
                              final String password)
  {
    try {
      return ApiClient.signin(username, password);
    } // try
    catch(Exception e) {
      return new Signin.Result(e.getMessage());
    } // catch
  } // Result

	/*
	<?xml version="1.0"?>
	<signin>
		<request>jezhiggins</request>
		<result>
			<id>8764</id>
			<username>jezhiggins</username>
			<email>jez@jezuk.co.uk</email>
			<name>Jez Higgins</name>
			<validatekey>0</validatekey>
			<validated>2011-01-17 12:43:15</validated>
			<privileges></privileges>
			<lastsignin>1304936768</lastsignin>
			<ip>82.35.229.11</ip>
			<deleted>no</deleted>
		</result>
	</signin>
	 */
  static public Factory<Result> factory() { 
    return new SigninFactory();
  } // factory

  static private class SigninFactory extends Factory<Signin.Result>
  {    
    private Signin.Result result_;
    
    @Override
    protected ContentHandler contentHandler()
    {
      result_ = new Result();
      
      final RootElement root = new RootElement("signin");
      final Element name = root.getChild("result").getChild("name");
      final Element email = root.getChild("result").getChild("email");
      final Element validated = root.getChild("result").getChild("validated");

      name.setEndTextElementListener(new EndTextElementListener() {
        public void end(String body) { result_.name_ = body; }
      });
      email.setEndTextElementListener(new EndTextElementListener() {
        public void end(String body) { result_.email_ = body; }
      });
      validated.setEndTextElementListener(new EndTextElementListener() {
        public void end(String body) { result_.validated_ = body; }
      });
      
      return root.getContentHandler();
    } // contentHandler

    @Override
    protected Signin.Result get()
    {
      return result_;
    } // get
  } // class SigninFactory
} // class Signin
