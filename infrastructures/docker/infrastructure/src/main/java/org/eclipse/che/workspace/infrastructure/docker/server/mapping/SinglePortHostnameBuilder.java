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
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/**
 * Produces hostnames in form:
 * server-<serverName>.<machineName>.<workspaceId>.<external_or_internal_address>.nip.io
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class SinglePortHostnameBuilder {

  private final String externalAddress;
  private final String internalAddress;

  public SinglePortHostnameBuilder(String externalAddress, String internalAddress) {
    this.externalAddress = externalAddress;
    this.internalAddress = internalAddress;
  }

  public String build(String serverName, String machineName, String workspaceID)
      throws InfrastructureException {
    StringJoiner joiner = new StringJoiner(".");
    if (serverName != null) {
      joiner.add("server-" + serverName.replace('/', '-'));
    }
    if (machineName != null) {
      joiner.add(machineName);
    }
    if (workspaceID != null) {
      joiner.add(workspaceID);
    }
    joiner.add(
        externalAddress != null
            ? getWildcardNipDomain(externalAddress)
            : getWildcardNipDomain(internalAddress));
    return joiner.toString();
  }

  /**
   * Gets a Wildcard domain based on the ip using an external provider nip.io
   *
   * @return wildcard domain
   */
  private String getWildcardNipDomain(String localAddress) {
    return String.format("%s.%s", getExternalIp(localAddress), "nip.io");
  }

  private String getExternalIp(String externalAddress) {
    try {
      return InetAddress.getByName(externalAddress).getHostAddress();
    } catch (UnknownHostException e) {
      throw new UnsupportedOperationException(
          "Unable to find the IP for the address '" + externalAddress + "'", e);
    }
  }
}
