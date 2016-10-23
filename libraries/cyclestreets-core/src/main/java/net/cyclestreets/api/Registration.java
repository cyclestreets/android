package net.cyclestreets.api;

import net.cyclestreets.core.R;

import java.io.IOException;

public class Registration {
  public static Result ok() {
    return new Result(ApiClient.context().getString(R.string.registration_ok));
  }

  public static Result error(String errorMessage) {
    return new Result(ApiClient.context().getString(R.string.registration_error_prefix), errorMessage);
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
