package net.cyclestreets.api;

import java.io.IOException;

public class Feedback {
  static public class Result {
    private final boolean ok;
    private final String message;

    public Result(String errorMessage) {
      ok = false;
      message = errorMessage;
    }

    public Result() {
      ok = true;
      message = "Thank you for submitting this feedback. We will get back to you when we have checked this out.";
    }

    public boolean ok() {
      return ok;
    }

    public String message() {
      return message;
    }
  }

  public static Feedback.Result send(final int itinerary,
                                     final String comments,
                                     final String name,
                                     final String email) throws IOException {
    return ApiClient.sendFeedback(itinerary, comments, name, email);
  }
}
