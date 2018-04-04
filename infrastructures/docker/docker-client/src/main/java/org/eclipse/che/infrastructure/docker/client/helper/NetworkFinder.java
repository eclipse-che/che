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

import com.google.inject.ImplementedBy;
import java.net.InetAddress;
import java.util.Optional;

/**
 * Provides helper method for the network
 *
 * @author Florent Benoit
 */
@ImplementedBy(DefaultNetworkFinder.class)
public interface NetworkFinder {

  /**
   * Gets the first inet address of a given network interface
   *
   * @param bridgeName name of the network interface
   * @return only ipv4 ip of the given bridge if found
   */
  Optional<InetAddress> getIPAddress(String bridgeName);

  /**
   * Search if a given network interface is matching the given subnet If there is a match, returns
   * the InetAddress
   *
   * @param subnet the first digits of an ip address. Like 123.123.123
   * @return optional ipv4 internet address if there was a matching one
   */
  Optional<InetAddress> getMatchingInetAddress(String subnet);
}
