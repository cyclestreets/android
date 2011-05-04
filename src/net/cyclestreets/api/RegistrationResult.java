package net.cyclestreets.api;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class RegistrationResult 
{
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
	
	public RegistrationResult() { }
	public RegistrationResult(final String message)
	{
		result = new Result();
		result.code = "0";
		result.message = message;
	} // RegistrationResult
	
	public boolean ok() { return "1".equals(result.code); }
	
	public String message() 
	{
		if(ok())
			return "Your account has been registered.\n\nAn email has been sent to the address you gave.\n\nWhen the email arrives, follow the instructions it contains to complete the registration.";
		return "Your account could not be registered.\n\n" + result.message; 
	} // message	
	
	@Element(required=false)
	private Result result;
	
	@Root
	public static class Result
	{
		@Element(required=false)
		public String code, message;
	} // class Result

} // class RegistrationResult
