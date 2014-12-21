package net.cyclestreets.api;

public final class UserJourney {
  private final String name_;
  private final int id_;

  UserJourney(final String name, final int id) {
    name_ = name;
    id_ = id;
  } // UserJourney

  public String name() { return name_; }
  public int id() { return id_; }
} // UserJourney
