package net.cyclestreets.api;

import org.osmdroid.api.IGeoPoint;
import org.xml.sax.ContentHandler;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;

public class Upload 
{
  static public class Result
  {
    public Result() { }
    public Result(final String error)
    {
      error_ = error;
    } // UploadResult

    public boolean ok() { return url_ != null; }
    public String url() { return url_; }     
    public String error() { return error_; }
    
    private String url_;
    private String error_;
  } // class Result

  static public Upload.Result photo(final String filename,
                                    final String username,
                                    final String password,
                                    final IGeoPoint location,
                                    final String metaCat,
                                    final String category,
                                    final String dateTime,
                                    final String caption)
          throws Exception
  {
    return ApiClient.uploadPhoto(filename, 
                                 username, 
                                 password, 
                                 location.getLongitudeE6() / 1E6,
                                 location.getLatitudeE6() / 1E6,
                                 metaCat, 
                                 category, 
                                 dateTime, 
                                 caption);
  } // photo
  
  ///////////////////////////////////////////////////
  static public Factory<Result> factory() { 
    return new UploadFactory();
  } // factory

  static private class UploadFactory extends Factory<Upload.Result>
  {    
    private Upload.Result result_;
    
    @Override
    protected ContentHandler contentHandler()
    {
      result_ = new Result();
      
      final RootElement root = new RootElement("addphoto");
      final Element url = root.getChild("result").getChild("url");
      final Element error = root.getChild("error").getChild("message");

      url.setEndTextElementListener(new EndTextElementListener() {
        public void end(String body) { result_.url_ = body; }
      });
      error.setEndTextElementListener(new EndTextElementListener() {
        public void end(String body) { result_.error_ = body; }
      });
      
      return root.getContentHandler();
    } // contentHandler

    @Override
    protected Upload.Result get()
    {
      return result_;
    } // get
  } // class UploadFactory
} // class UploadResult
