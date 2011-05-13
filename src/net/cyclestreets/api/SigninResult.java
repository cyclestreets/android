package net.cyclestreets.api;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

public class SigninResult 
{
	public SigninResult() { }
	public SigninResult(final String message)
	{
		result = new Result();
		result.validated = "";
		result.error = message;
	} // SigninResult
	
	public boolean ok() { return result != null && result.validated.length() != 0; }
	
	public String email() { return result.email; }
	public String name() { return result.name; }
	
	public String error() { return result.error; }
	
	
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
		
	@Element(required=false)
	private Result result;
	@SuppressWarnings("unused")
	@Element(required=false)
	private String request;
	
	@Root
	public static class Result
	{
		@Element(required=false)
		public String id, username, email, name, validatekey;
		@Element(required=false)
		public String validated, privileges, lastsignin, ip, deleted; 
		
		@Element(required=false)
		public String error; // this is a bit of an abuse, really
	} // class Result

} // class SigninResult
