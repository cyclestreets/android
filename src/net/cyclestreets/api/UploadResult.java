package net.cyclestreets.api;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Element;

@Root(strict=false)
public class UploadResult 
{
	/*
	<?xml version="1.0"?>
	<addphoto>
		<request><datetime>1301347930</datetime></request>
		<error/>
		<result>
			<url>http://www.cyclestreets.net/location/29451/</url>
			<imageUrl>http://www.cyclestreets.net/location/29451/cyclestreets29451.jpg</imageUrl>
			<thumbnailUrl>http://www.cyclestreets.net/location/29451/cyclestreets29451-size425.jpg</thumbnailUrl>
			<thumbnailSizes>60|120|150|180|200|250|300|350|400|400|425|450|500|640</thumbnailSizes>
		</result>
	</addphoto>
   */

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
