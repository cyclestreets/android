package net.cyclestreets.api;

import net.cyclestreets.core.R;

import org.osmdroid.api.IGeoPoint;

public class Upload {
  public static class Result extends net.cyclestreets.api.Result {
    private String url;

    public static Result forUrl(String url) {
      return new Result(url);
    }

    public static Result error(String error) {
      return new Result(ApiClient.INSTANCE.getMessage(R.string.upload_error_prefix), error);
    }

    private Result(String url) {
      super(ApiClient.INSTANCE.getMessage(R.string.upload_ok));
      this.url = url;
    }

    private Result(String prefix, String error) {
      super(prefix, error);
    }

    public String url() { return url; }
  }

  public static Upload.Result photo(final String filename,
                                    final String username,
                                    final String password,
                                    final IGeoPoint location,
                                    final String metaCat,
                                    final String category,
                                    final String dateTime,
                                    final String caption) {
    return ApiClient.INSTANCE.uploadPhoto(filename,
                                          username,
                                          password,
                                          location.getLongitude(),
                                          location.getLatitude(),
                                          metaCat,
                                          category,
                                          dateTime,
                                          caption);
  }
}
