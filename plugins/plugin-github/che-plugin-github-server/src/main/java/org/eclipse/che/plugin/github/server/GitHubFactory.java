package org.eclipse.che.plugin.github.server;


import java.io.IOException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.kohsuke.github.GitHub;

/**
 * Factory class used to generate connection to GitHub
 *
 * @author Igor Vinokur
 * @author Sergii Leschenko
 */
public class GitHubFactory {

  /**
   * Connect to GitHub API using OAuth
   *
   * @param oauthToken  token for OAuth connection
   * @return connected GitHub API class
   */
  public GitHub oauthConnect(String oauthToken) throws ServerException, UnauthorizedException {
    try {
      return GitHub.connectUsingOAuth(oauthToken);
    } catch (IOException e) {
      throw new ServerException(e.getMessage());
    }
  }
}
