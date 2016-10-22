package net.cyclestreets.api;

import org.osmdroid.api.IGeoPoint;

import java.io.IOException;

public class Upload {
  public static class Result extends net.cyclestreets.api.Result {
    private String url;

    private static final String okMessage = "You have successfully signed into CycleStreets.";
    private static final String errorPrefix = "There was a problem uploading your photo: \n";

    public static Result forUrl(String url) {
      return new Result(url);
    }

    public static Result error(String error) {
      return new Result(errorPrefix, error);
    }

    private Result(String url) {
      super(okMessage);
      this.url = url;
    }

    private Result(String prefix, String error) {
      super(prefix, error);
    }

    public String url() { return url; }
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
