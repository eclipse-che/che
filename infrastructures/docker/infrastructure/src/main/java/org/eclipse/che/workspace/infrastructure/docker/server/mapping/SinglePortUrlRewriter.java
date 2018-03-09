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
  private final String cheHostProtocol;

  @Inject
  public SinglePortUrlRewriter(
      @Named("che.port") int chePort,
      @Named("che.host.protocol") String cheHostProtocol,
      Provider<SinglePortHostnameBuilder> hostnameBuilderProvider) {
    this.chePort = chePort;
    this.cheHostProtocol = cheHostProtocol;
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
      //      if ("https".equals(cheHostProtocol)) {
      //
      //          if (url.contains("https://") || url.contains("wss://")) {
      //        	  uriBUilder.port(443);
      //          }
      //
      //      }
      //      if (chePort != 80 && chePort != 443) {
      //          uriBUilder.port(chePort);
      //        }
      url = uriBUilder.build().toString();
      if ("https".equals(cheHostProtocol)) {
        url = url.replace("ws://", "wss://");
        url = url.replace("http://", "https://");
      }
      // replace ports for anything passing through traefik (e.g. http, https, ws, wss)
      if (url.contains("http://")
          || url.contains("https://")
          || url.contains("ws://")
          || url.contains("wss://")) {
        url = UriBuilder.fromUri(url).port(chePort).build().toString();
      }
    } catch (UriBuilderException | IllegalArgumentException e) {
      throw new InternalInfrastructureException(
          format(
              "Rewriting of host '%s' in URL '%s' failed. Error: %s", host, url, e.getMessage()));
    }

    return url;
  }
}
