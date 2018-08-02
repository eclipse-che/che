/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages and cache MavenServerWrapper instances
 *
 * @author Evgen Vidolob
 */
@Singleton
public class MavenWrapperManager {

  private final MavenServerManager serverManager;
  private final Map<ServerType, MavenServerWrapper> cache = new HashMap<>();
  private final Set<MavenServerWrapper> usedServers = new HashSet<>();

  @Inject
  public MavenWrapperManager(MavenServerManager serverManager) {
    this.serverManager = serverManager;
  }

  public synchronized MavenServerWrapper getMavenServer(ServerType type) {
    MavenServerWrapper wrapper = cache.get(type);
    if (wrapper == null) {
      wrapper = serverManager.createMavenServer();
      cache.put(type, wrapper);
    }

    if (usedServers.contains(wrapper)) {
      // need to warn here
      return serverManager.createMavenServer();
    }

    usedServers.add(wrapper);
    return wrapper;
  }

  public synchronized void release(MavenServerWrapper wrapper) {
    if (usedServers.contains(wrapper)) {
      wrapper.reset();
      usedServers.remove(wrapper);
    } else {
      wrapper.dispose();
    }
  }

  public enum ServerType {
    RESOLVE,
    DOWNLOAD
  }
}
