package net.cyclestreets.api;


import org.simpleframework.xml.Root;
import org.simpleframework.xml.Element;

@Root(strict=false)
public class UploadResult 
{
	public UploadResult () { }
	public UploadResult (final String message)
	{
		error = new Error();
		error.message = message;
	} // UploadResult

	public boolean ok() { return result != null && result.url != null; }
	public String url() { return result.url; }	   
	public String errorMessage() { return error.message; }
	   
	@Element(required=false)
	private Result result;
   
	@Element(required=false)
	private Error error;
   
	@Root
	public static class Result
	{
		@Element(required=false)
		public String url, imageUrl, thumbnailUrl, thumbnailSizes;
	} // class Result
   
	@Root
	public static class Error
	{
		@Element(required=false)
		public String code, message;
	} // class Error
} // class UploadResult
