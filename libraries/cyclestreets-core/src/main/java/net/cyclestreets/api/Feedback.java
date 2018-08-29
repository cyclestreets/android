package net.cyclestreets.api;

import net.cyclestreets.core.R;

public class Feedback {
  public static Result ok() {
    return new Result(ApiClient.INSTANCE.getMessage(R.string.feedback_ok));
  }

  public static Result error(String errorMessage) {
    return new Result(ApiClient.INSTANCE.getMessage(R.string.feedback_error_prefix), errorMessage);
  }

  //////////////////////////////////////////////////////
  public static Result send(final int itinerary,
                            final String comments,
                            final String name,
                            final String email) {
    try {
      return ApiClient.INSTANCE.sendFeedback(itinerary, comments, name, email);
    } catch (Exception e) {
      return error(e.getMessage());
    }
  }
}
