package net.minecraft.launcher.authentication.yggdrasil;

public class AuthenticationRequest
{
  private Agent agent;
  private String username;
  private String password;
  private String clientToken;

  public AuthenticationRequest(YggdrasilAuthenticationService authenticationService, String password)
  {
    this.agent = authenticationService.getAgent();
    this.username = authenticationService.getUsername();
    this.clientToken = authenticationService.getClientToken();
    this.password = password;
  }
}