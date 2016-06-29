package net.cyclestreets.api;

public class Registration
{
  static public class Result {
    boolean ok_;
    String message_;

    public Result() {
      ok_ = false;
      message_ = "Unknown reason";
    }
    
    public Result(final boolean ok, final String message) {
      ok_ = ok;
      message_ = message;
    }
    
    public boolean ok() { return ok_; }

    public String message() {
      if (ok_)
        return "Your account has been registered.\n\nAn email has been sent to the address you gave.\n\nWhen the email arrives, follow the instructions it contains to complete the registration.";
      return "Your account could not be registered.\n\n" + message_; 
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
    catch(Exception e) {
      return new Result(false, e.getMessage());
    }
  }
}
