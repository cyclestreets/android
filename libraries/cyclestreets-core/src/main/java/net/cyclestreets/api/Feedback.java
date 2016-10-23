package net.cyclestreets.api;

import net.cyclestreets.core.R;

import java.io.IOException;

public class Feedback {
  public static Result ok() {
    return new Result(ApiClient.context().getString(R.string.feedback_ok));
  }

  public static Result error(String errorMessage) {
    return new Result(ApiClient.context().getString(R.string.feedback_error_prefix), errorMessage);
  }

  //////////////////////////////////////////////////////
  public static Result send(final int itinerary,
                            final String comments,
                            final String name,
                            final String email) {
    try {
      return ApiClient.sendFeedback(itinerary, comments, name, email);
    } catch (IOException e) {
      return error(e.getMessage());
    }
  }
}
