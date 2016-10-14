package net.cyclestreets.api;

public class Signin
{
  public static class Result {
    private final boolean success;
    private String name;
    private String email;
    private String error;

    public Result(String name, String email) {
      this.success = true;
      this.name = name;
      this.email = email;
    }

    public Result(final String errorMessage) {
      this.success = false;
      this.error = errorMessage;
    }
    
    public boolean ok() {
      return success;
    }
    
    public String email() { return email; }
    public String name() { return name; }
    
    public String error() {
      if (error != null)
        return "Error : " + error;
      return "Unknown error";
    }
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
