package net.cyclestreets.api;

import java.io.IOException;

public class Registration
{
  static public class Result {
    private final boolean ok;
    private final String errorMessage;

    public Result() {
      ok = false;
      errorMessage = "Unknown reason";
    }
    
    public Result(final boolean ok, final String errorMessage) {
      this.ok = ok;
      this.errorMessage = errorMessage;
    }
    
    public boolean ok() { return ok; }

    public String message() {
      if (ok) {
        return "Your account has been registered.\n\nAn email has been sent to the address you gave.\n\nWhen the email arrives, follow the instructions it contains to complete the registration.";
      }
      return "Your account could not be registered.\n\n" + errorMessage;
    }
  }
  
  //////////////////////////////////////////////////////
  static public Result register(final String username, 
                                final String password,
                                final String name,
                                final String email) {
    try {
      return ApiClient.register(username, password, name, email);
    }
    catch (IOException e) {
      return new Result(false, e.getMessage());
    }
  }
}
