/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.server.mapping;

import static java.lang.String.format;

import java.net.URI;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * {@link URLRewriter} used in case when single port mode is on. Rewrites host in original URL as
 * provided by {@link SinglePortHostnameBuilder}
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class SinglePortUrlRewriter implements URLRewriter {

  private final SinglePortHostnameBuilder hostnameBuilder;
  private final String wildcartPort;
  private final int chePort;

  @Inject
  public SinglePortUrlRewriter(
      @Named("che.docker.ip") String internalIpOfContainers,
      @Named("che.port") int chePort,
      @Nullable @Named("che.docker.ip.external") String externalIpOfContainers,
      @Nullable @Named("che.singleport.wildcard_domain.host") String wildcardHost,
      @Nullable @Named("che.singleport.wildcard_domain.port") String wildcardPort) {
    this.hostnameBuilder =
        new SinglePortHostnameBuilder(externalIpOfContainers, internalIpOfContainers, wildcardHost);
    this.chePort = chePort;
    this.wildcartPort = wildcardPort;
  }

  @Override
  public String rewriteURL(
      @Nullable RuntimeIdentity identity,
      @Nullable String machineName,
      @Nullable String serverName,
      String url)
      throws InfrastructureException {
    final String host = hostnameBuilder.build(serverName, machineName, identity.getWorkspaceId());
    try {
      int port = wildcartPort != null ? Integer.parseInt(wildcartPort) : chePort;
      URI uri = UriBuilder.fromUri(url).host(host).port(port).build();
      url = uri.toString();
    } catch (UriBuilderException | IllegalArgumentException e) {
      throw new InternalInfrastructureException(
          format(
              "Rewriting of host '%s' in URL '%s' failed. Error: %s", host, url, e.getMessage()));
    }
    return url;
  }
}
