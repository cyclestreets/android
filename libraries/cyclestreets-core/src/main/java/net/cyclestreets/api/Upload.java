package net.cyclestreets.api;

import net.cyclestreets.core.R;

import org.osmdroid.api.IGeoPoint;

import java.io.IOException;

public class Upload {
  public static class Result extends net.cyclestreets.api.Result {
    private String url;

    public static Result forUrl(String url) {
      return new Result(url);
    }

    public static Result error(String error) {
      return new Result(ApiClient.context().getString(R.string.upload_error_prefix), error);
    }

    private Result(String url) {
      super(ApiClient.context().getString(R.string.upload_ok));
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
