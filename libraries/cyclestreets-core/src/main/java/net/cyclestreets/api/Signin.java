package net.cyclestreets.api;

public class Signin {
  public static class Result extends net.cyclestreets.api.Result {
    private String name;
    private String email;

    private static final String okMessage = "You have successfully signed into CycleStreets.";
    private static final String errorPrefix = "Error : ";
    private static final String defaultError = "Could not sign into CycleStreets.  Please check your username and password.";

    public static Result forNameAndEmail(String name, String email) {
      return new Result(name, email);
    }

    public static Result error(String error) {
      return new Result(error);
    }

    private Result(String name, String email) {
      super(okMessage);
      this.name = name;
      this.email = email;
    }

    private Result(final String error) {
      super(errorPrefix, error != null ? error : defaultError);
    }
    
    public String email() { return email; }
    public String name() { return name; }
  } // class Result
  
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
