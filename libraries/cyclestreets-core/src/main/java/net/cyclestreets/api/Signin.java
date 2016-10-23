package net.cyclestreets.api;

import net.cyclestreets.core.R;

public class Signin {
  public static class Result extends net.cyclestreets.api.Result {
    private String name;
    private String email;

    public static Result forNameAndEmail(String name, String email) {
      return new Result(name, email);
    }

    public static Result error(String error) {
      return new Result(error);
    }

    private Result(String name, String email) {
      super(ApiClient.context().getString(R.string.signin_ok));
      this.name = name;
      this.email = email;
    }

    private Result(final String error) {
      super(ApiClient.context().getString(R.string.signin_error_prefix),
            error != null ? error : ApiClient.context().getString(R.string.signin_default_error));
    }
    
    public String email() { return email; }
    public String name() { return name; }
  }
  
  public static Result signin(final String username,
                              final String password) {
    try {
      return ApiClient.signin(username, password);
    }
    catch (Exception e) {
      return new Signin.Result(e.getMessage());
    }
  }
}
