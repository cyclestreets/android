package net.cyclestreets.api;

import java.io.IOException;

public class Feedback {
  private static final String okMessage = "Thank you for submitting this feedback. We will get back to you when we have checked this out.";
  private static final String errorPrefix = "Your feedback could not be sent.";

  public static Result ok() {
    return new Result(okMessage);
  }

  public static Result error(String errorMessage) {
    return new Result(errorPrefix, errorMessage);
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
