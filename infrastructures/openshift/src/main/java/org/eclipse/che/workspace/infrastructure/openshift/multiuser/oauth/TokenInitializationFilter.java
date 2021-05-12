/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.client.OpenShiftClient;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.authentication.commons.SessionStore;
import org.eclipse.che.multiuser.api.authentication.commons.filter.MultiUserEnvironmentInitializationFilter;
import org.eclipse.che.multiuser.api.authentication.commons.token.RequestTokenExtractor;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TokenInitializationFilter extends MultiUserEnvironmentInitializationFilter {
  private static final Logger LOG = LoggerFactory.getLogger(TokenInitializationFilter.class);

  private static final List<String> UNAUTHORIZED_ENDPOINT_PATHS =
      Collections.singletonList("/system/state");

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
    } catch (KubernetesClientException e) {
      if (e.getCode() == 401) {
        LOG.error(
            "Unauthorized when getting current user. Invalid OpenShift token, probably expired. Re-login? Re-request the token?");
      }
      throw new RuntimeException(e);
    }
  }

  @Override
  protected Subject extractSubject(String token) {
    try {
      OpenShiftClient client = clientFactory.createAuthenticatedOC(token);
      User openshiftUser = client.currentUser();
      ObjectMeta userMeta = openshiftUser.getMetadata();
      return new SubjectImpl(userMeta.getName(), userMeta.getUid(), token, false);
    } catch (InfrastructureException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void handleMissingToken(
      ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    // if request path is in unauthorized endpoints, continue
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      String path = httpRequest.getServletPath();
      if (UNAUTHORIZED_ENDPOINT_PATHS.contains(path)) {
        LOG.debug("Allowing request to '{}' without authorization header.", path);
        chain.doFilter(request, response);
        return;
      }
    }

    LOG.error("Rejecting the request due to missing token in Authorization header.");
    sendError(response, 401, "Authorization token is missing");
  }

  @Override
  public void init(FilterConfig filterConfig) {
    LOG.debug("TokenInitializationFilter#init({})", filterConfig);
  }

  @Override
  public void destroy() {
    LOG.debug("TokenInitializationFilter#destroy()");
  }
}
