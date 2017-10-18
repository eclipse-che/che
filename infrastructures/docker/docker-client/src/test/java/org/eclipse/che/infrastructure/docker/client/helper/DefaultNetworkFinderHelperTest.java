/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.helper;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import org.testng.annotations.Test;

/**
 * Test the network finder
 *
 * @author Florent Benoit
 */
public class DefaultNetworkFinderHelperTest {

  /**
   * Check that we can find ipv4 address if we have some bridge
   *
   * @throws SocketException
   */
  @Test
  public void checkFoundIpForABridge() throws SocketException {

    DefaultNetworkFinder networkFinder = new DefaultNetworkFinder();

    Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
    while (enumNetworkInterfaces.hasMoreElements()) {
      NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
      Optional<InetAddress> foundIpAddress = networkFinder.getIPAddress(networkInterface.getName());

      Enumeration<InetAddress> enumAddresses = networkInterface.getInetAddresses();
      List<InetAddress> list = new ArrayList<>();
      while (enumAddresses.hasMoreElements()) {
        InetAddress inetAddress = enumAddresses.nextElement();
        if (inetAddress instanceof Inet4Address) {
          list.add(inetAddress);
        }
      }
      if (list.size() > 0) {
        assertTrue(foundIpAddress.isPresent());
        assertTrue(list.contains(foundIpAddress.get()));
      }
    }
  }

  /**
   * Check that we can find a network ip address by having the subnet
   *
   * @throws SocketException
   */
  @Test
  public void checkMatchingSubnet() throws SocketException, UnknownHostException {

    DefaultNetworkFinder networkFinder = new DefaultNetworkFinder();

    InetAddress loopBack = InetAddress.getLoopbackAddress();
    if (loopBack instanceof Inet4Address) {
      Optional<InetAddress> matchingAddress =
          networkFinder.getMatchingInetAddress(
              loopBack.getHostAddress().substring(0, loopBack.getHostAddress().lastIndexOf('.')));

      assertTrue(matchingAddress.isPresent());
      assertEquals(matchingAddress.get().getHostAddress(), loopBack.getHostAddress());
    }
  }
}
