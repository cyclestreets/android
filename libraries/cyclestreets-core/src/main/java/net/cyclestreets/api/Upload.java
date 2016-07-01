package net.cyclestreets.api;

import org.osmdroid.api.IGeoPoint;

import java.io.IOException;

public class Upload {
  public static class Result {
    private String url;
    private String error;

    public static Result forError(final String error) {
      Result result = new Result();
      result.error = error;
      return result;
    }

    public static Result forUrl(final String url) {
      Result result = new Result();
      result.url = url;
      return result;
    }

    public boolean ok() { return url != null; }
    public String url() { return url; }
    public String error() { return error; }
  }

  static public Upload.Result photo(final String filename,
                                    final String username,
                                    final String password,
                                    final IGeoPoint location,
                                    final String metaCat,
                                    final String category,
                                    final String dateTime,
                                    final String caption) throws IOException {
    return ApiClient.uploadPhoto(filename, 
                                 username, 
                                 password, 
                                 location.getLongitudeE6() / 1E6,
                                 location.getLatitudeE6() / 1E6,
                                 metaCat, 
                                 category, 
                                 dateTime, 
                                 caption);
  }
}
