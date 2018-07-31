/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.server.mapping;

import static java.lang.String.format;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
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

  private final Provider<SinglePortHostnameBuilder> hostnameBuilderprovider;
  private final int chePort;

  @Inject
  public SinglePortUrlRewriter(
      @Named("che.port") int chePort, Provider<SinglePortHostnameBuilder> hostnameBuilderProvider) {
    this.chePort = chePort;
    this.hostnameBuilderprovider = hostnameBuilderProvider;
  }

  @Override
  public String rewriteURL(
      @Nullable RuntimeIdentity identity,
      @Nullable String machineName,
      @Nullable String serverName,
      String url)
      throws InfrastructureException {
    final String host =
        hostnameBuilderprovider.get().build(serverName, machineName, identity.getWorkspaceId());
    try {
      UriBuilder uriBUilder = UriBuilder.fromUri(url).host(host);
      if (chePort != 80 && chePort != 443) {
        uriBUilder.port(chePort);
      }
      url = uriBUilder.build().toString();
    } catch (UriBuilderException | IllegalArgumentException e) {
      throw new InternalInfrastructureException(
          format(
              "Rewriting of host '%s' in URL '%s' failed. Error: %s", host, url, e.getMessage()));
    }
    return url;
  }
}
