package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.cyclestreets.api.Signin;

public class UserAuthenticateResponseDto extends ApiResponseDto {
  @JsonProperty
  private String name;
  @JsonProperty
  private String email;
  @JsonProperty
  private String username;
  @JsonProperty
  private String[] privileges;

  public Signin.Result toSigninResult() {
    return wasSuccessful() ? Signin.Result.forNameAndEmail(name, email) : Signin.Result.error(error);
  }
}
