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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringJoiner;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * {@link URLRewriter} used in case when single port mode is on. Produces URL in form:
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class SinglePortUrlRewriter implements URLRewriter {

  private final String externalAddress;

  @Inject
  public SinglePortUrlRewriter(
      @Nullable @Named("che.docker.ip.external") String externalIpOfContainers) {
    this.externalAddress = externalIpOfContainers;
  }

  @Override
  public String rewriteURL(
      @Nullable RuntimeIdentity identity,
      @Nullable String machineName,
      @Nullable String serverName,
      String url)
      throws InfrastructureException {
    StringJoiner joiner = new StringJoiner(".");
    if (serverName != null) {
      joiner.add("server-" + serverName);
    }
    if (machineName != null) {
      joiner.add(machineName);
    }
    if (identity != null) {
      joiner.add(identity.getWorkspaceId());
    }
    joiner.add(getWildcardNipDomain());
    return joiner.toString();
  }

  /**
   * Gets a Wildcard domain based on the ip using an external provider nip.io
   *
   * @return wildcard domain
   */
  protected String getWildcardNipDomain() {
    return String.format("%s.%s", getExternalIp(externalAddress), "nip.io");
  }

  protected String getExternalIp(String externalAddress) {
    try {
      return InetAddress.getByName(externalAddress).getHostAddress();
    } catch (UnknownHostException e) {
      throw new UnsupportedOperationException(
          "Unable to find the IP for the address '" + externalAddress + "'", e);
    }
  }
}
