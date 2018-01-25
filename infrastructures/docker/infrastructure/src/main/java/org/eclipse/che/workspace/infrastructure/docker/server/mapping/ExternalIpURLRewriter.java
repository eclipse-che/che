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

import com.google.inject.Inject;
import java.net.URI;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.docker.DockerRuntimeInfrastructure;

/**
 * {@link URLRewriter} needed for {@link DockerRuntimeInfrastructure} to handle specific cases of
 * running containers. </br> For example, containers in MacOS, Windows, etc.
 *
 * @author Alexander Garagatyi
 */
public class ExternalIpURLRewriter implements URLRewriter {
  static final String EXTERNAL_IP_PROPERTY = "che.docker.ip.external";

  private final String externalIpOfContainers;

  /**
   * `che.docker.ip.external` defines containers external IP in case it is needed For example on
   * Docker for Mac external IP of container is `localhost`. On Windows it can be either `localhost`
   * or hosts IP (but hosts IP may change in case of moving from one network to another).
   */
  @Inject
  public ExternalIpURLRewriter(
      @Nullable @Named(EXTERNAL_IP_PROPERTY) String externalIpOfContainers) {
    this.externalIpOfContainers = externalIpOfContainers;
  }

  @Override
  public String rewriteURL(
      @Nullable RuntimeIdentity identity,
      @Nullable String machineName,
      @Nullable String serverName,
      String url)
      throws InfrastructureException {

    if (externalIpOfContainers != null) {
      try {
        URI uri = UriBuilder.fromUri(url).host(externalIpOfContainers).build();
        url = uri.toString();
      } catch (UriBuilderException | IllegalArgumentException e) {
        throw new InternalInfrastructureException(
            format(
                "Rewriting of host '%s' in URL '%s' failed. Error: %s",
                externalIpOfContainers, url, e.getMessage()));
      }
    }
    return url;
  }
}
