package org.eclipse.che.security.oauth1;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.factory.server.AuthenticationLocationComposer;

public class BitbucketServerAuthenticationLocationComposer implements
    AuthenticationLocationComposer {

  private final String apiEndpoint;

  @Inject
  public BitbucketServerAuthenticationLocationComposer(@Named("che.api") String apiEndpoint) {
   this.apiEndpoint = apiEndpoint;
  }

  @Override
  public String composeLocation(String redirectAfterLogin) {
    return apiEndpoint + "/oauth/1.0/authenticate?oauth_provider=bitbucket-server&request_method=POST&signature_method=rsa&redirect_after_login=" + redirectAfterLogin;
  }
}
