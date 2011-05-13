package net.cyclestreets.api;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

public class FeedbackResult 
{
	/* <feedback>
	 * 	 <request>713240</request>
	 *   <result>
	 * 	   <code>1</code>
	 *     <message>Thank you for submitting this feedback. We will 
	 *      get back to you when we have checked this out.</message>
	 *     <url>http://www.cyclestreets.net/journey/713240/</url>
	 *     <feedbackid>4267</feedbackid>
	 *   </result>
	 *   </feedback>
	 */

	public boolean ok() { return "1".equals(result.code); }
	public String message() { return result.message; }
	
	@Element(required=false)
	private Result result;
	@SuppressWarnings("unused")
	@Element(required=false)
	private String request;
	
	@Root
	public static class Result
	{
		@Element(required=false)
		public String code, message, url, feedbackid;
	} // Result
} // class FeedbackResult
