package net.cyclestreets.api;

public class Result {

  private String okMessage;
  private String errorPrefix;
  private boolean ok;
  private String errorMessage;

  Result(String okMessage) {
    this.ok = true;
    this.okMessage = okMessage;
  }

  Result (String errorPrefix, String errorMessage) {
    this.ok = false;
    this.errorPrefix = errorPrefix;
    this.errorMessage = errorMessage;
  }

  public final boolean ok() { return ok; }

  public final String message() {
    return (ok) ? okMessage : errorPrefix + errorMessage;
  }
}
