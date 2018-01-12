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
package org.eclipse.che.infrastructure.docker.client.helper;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Optional;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Implementation of the {@link NetworkFinder}
 *
 * @author Florent Benoit
 */
@Singleton
public class DefaultNetworkFinder implements NetworkFinder {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultNetworkFinder.class);

  /**
   * Gets the first Inet address of a given network interface if it's found
   *
   * @param bridgeName name of the network interface
   * @return only ipv4 ip of the given bridge
   */
  @Override
  public Optional<InetAddress> getIPAddress(String bridgeName) {

    NetworkInterface docker0 = null;
    try {
      docker0 = NetworkInterface.getByName(bridgeName);
    } catch (SocketException e) {
      LOG.error("Unable to list the network interfaces", e);
    }

    if (docker0 != null) {
      // first ipv4 ip
      Enumeration<InetAddress> addresses = docker0.getInetAddresses();
      while (addresses.hasMoreElements()) {
        InetAddress inetAddress = addresses.nextElement();
        if (inetAddress instanceof Inet4Address) {
          return Optional.of(inetAddress);
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Search if a given network interface is matching the given subnet If there is a match, returns
   * the InetAddress
   *
   * @param subnet the first digits of an ip address. Like 123.123.123
   * @return optional ipv4 internet address if there was a matching one
   */
  @Override
  public Optional<InetAddress> getMatchingInetAddress(String subnet) {

    Enumeration<NetworkInterface> interfacesEnum = null;
    try {
      interfacesEnum = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException e) {
      LOG.error("Unable to list the network interfaces", e);
      return Optional.empty();
    }

    while (interfacesEnum.hasMoreElements()) {
      NetworkInterface networkInterface = interfacesEnum.nextElement();
      Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses();
      while (inetAddressEnumeration.hasMoreElements()) {
        InetAddress inetAddress = inetAddressEnumeration.nextElement();
        if (inetAddress instanceof Inet4Address) {
          if (inetAddress.getHostAddress().startsWith(subnet)) {
            return Optional.of(inetAddress);
          }
        }
      }
    }
    return Optional.empty();
  }
}
