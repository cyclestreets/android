package net.cyclestreets.api;

import java.io.IOException;

public class Registration {
  private static final String okMessage = "Your account has been registered.\n\nAn email has been sent to the address you gave.\n\nWhen the email arrives, follow the instructions it contains to complete the registration.";
  private static final String errorPrefix = "Your account could not be registered.\n\n";

  public static Result ok() {
    return new Result(okMessage);
  }

  public static Result error(String errorMessage) {
    return new Result(errorPrefix, errorMessage);
  }

  //////////////////////////////////////////////////////
  public static Result register(final String username,
                                final String password,
                                final String name,
                                final String email) {
    try {
      return ApiClient.register(username, password, name, email);
    } catch (IOException e) {
      return error(e.getMessage());
    }
  }
}
