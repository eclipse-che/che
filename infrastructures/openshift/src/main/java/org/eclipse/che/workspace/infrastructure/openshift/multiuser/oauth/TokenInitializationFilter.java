package org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth;

import io.fabric8.openshift.client.OpenShiftClient;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.authentication.commons.SessionStore;
import org.eclipse.che.multiuser.api.authentication.commons.filter.MultiUserEnvironmentInitializationFilter;
import org.eclipse.che.multiuser.api.authentication.commons.token.RequestTokenExtractor;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TokenInitializationFilter extends MultiUserEnvironmentInitializationFilter {
  private static final Logger LOG = LoggerFactory.getLogger(TokenInitializationFilter.class);

  private final OpenShiftClientFactory clientFactory;

  @Inject
  public TokenInitializationFilter(
      SessionStore sessionStore,
      RequestTokenExtractor tokenExtractor,
      OpenShiftClientFactory clientFactory) {
    super(sessionStore, tokenExtractor);
    this.clientFactory = clientFactory;
  }

  @Override
  protected String getUserId(String token) {
    try {
      OpenShiftClient client = clientFactory.createAuthenticatedOC(token);
      return client.currentUser().getMetadata().getUid();
    } catch (InfrastructureException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected Subject extractSubject(String token) throws ServletException {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  protected void handleMissingToken(
      ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    LOG.error("Missing token header.");
    sendError(response, 401, "Authorization token is missing");
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    LOG.debug("TokenInitializationFilter#init({})", filterConfig);
  }

  @Override
  public void destroy() {
    LOG.debug("TokenInitializationFilter#destroy()");
  }
}
