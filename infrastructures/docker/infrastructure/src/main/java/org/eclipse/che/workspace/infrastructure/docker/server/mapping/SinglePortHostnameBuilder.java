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
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.inject.ConfigurationException;

/**
 * Produces host names in form:
 * [serverName]-[machineName]-[workspaceId].<external_or_internal_address>.<wildcardNipDomain> If
 * some of the server name or machine name or workspace id is null, they will be not included.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class SinglePortHostnameBuilder {

  private final String wildcardDomain;

  /**
   * hostname labels may contain only the ASCII letters 'a' through 'z' (in a case-insensitive
   * manner), the digits '0' through '9', and the hyphen ('-').
   */
  private final Pattern pattern = Pattern.compile("[^a-zA-Z0-9\\-]");

  public SinglePortHostnameBuilder(
      String externalAddress, String internalAddress, String wildcardHost) {
    this.wildcardDomain =
        externalAddress != null
            ? getWildcardDomain(externalAddress, wildcardHost)
            : getWildcardDomain(internalAddress, wildcardHost);
  }

  /**
   * Constructs hostname from given params.
   *
   * @param serverName optional server name
   * @param machineName optional machine name
   * @param workspaceID optional workspace ID
   * @return composite hostname
   */
  public String build(String serverName, String machineName, String workspaceID) {
    StringJoiner joiner = new StringJoiner("-");
    if (serverName != null) {
      joiner.add(normalize(serverName));
    }
    if (machineName != null) {
      joiner.add(normalize(machineName));
    }
    if (workspaceID != null) {
      joiner.add(normalize(workspaceID));
    }
    return joiner.toString() + "." + wildcardDomain;
  }

  /**
   * Gets a Wildcard domain based on the ip using an external provider like nip.io or by providing
   * an IP-less DNS yourself
   *
   * @return wildcard domain
   */
  private String getWildcardDomain(String localAddress, String wildcardHost) {
    if (wildcardHost == null) {
      return String.format("%s.%s", getExternalIp(localAddress), "nip.io");
    } else if (wildcardHost.contains("nip.io") || wildcardHost.contains("xip.io")) {
      return String.format("%s.%s", getExternalIp(localAddress), wildcardHost);
    } else {
      // IP-less DNS
      return wildcardHost;
    }
  }

  private String getExternalIp(String localAddress) {
    try {
      return InetAddress.getByName(localAddress).getHostAddress();
    } catch (UnknownHostException e) {
      throw new UnsupportedOperationException(
          "Unable to find the IP for the address '" + localAddress + "'", e);
    }
  }

  private String normalize(String input) {
    String normalized = pattern.matcher(input).replaceAll("-");
    // Check not starts or ends with hyphen
    while (normalized.startsWith("-")) {
      normalized = normalized.substring(1);
    }
    while (normalized.endsWith("-")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }

  public static class SinglePortHostnameBuilderProvider
      implements Provider<SinglePortHostnameBuilder> {

    private final SinglePortHostnameBuilder instance;

    @Inject
    public SinglePortHostnameBuilderProvider(
        @Named("che.single.port") boolean isSinglePortEnabled,
        @Nullable @Named("che.docker.ip") String internalIpOfContainers,
        @Nullable @Named("che.docker.ip.external") String externalIpOfContainers,
        @Nullable @Named("che.singleport.wildcard_domain.host") String wildcardHost) {
      if (isSinglePortEnabled && internalIpOfContainers == null && externalIpOfContainers == null) {
        throw new ConfigurationException(
            "Value of both of the properties 'che.docker.ip' and 'che.docker.ip.external' is null,"
                + " which is unsuitable for the single-port mode");
      }
      this.instance =
          isSinglePortEnabled
              ? new SinglePortHostnameBuilder(
                  externalIpOfContainers, internalIpOfContainers, wildcardHost)
              : null;
    }

    @Override
    public SinglePortHostnameBuilder get() {
      if (instance == null) {
        throw new IllegalStateException(
            "This class may not be used other than in single-port mode.");
      }
      return instance;
    }
  }
}
