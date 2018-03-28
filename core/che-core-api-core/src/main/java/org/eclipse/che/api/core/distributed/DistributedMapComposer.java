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
package org.eclipse.che.api.core.distributed;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.blocks.ReplicatedHashMap;
import org.slf4j.Logger;

/**
 * Helps to compose JGroups-based replicated maps.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@Singleton
public class DistributedMapComposer {

  private static final Logger LOG = getLogger(DistributedMapComposer.class);

  private final String confFile;

  @Inject
  public DistributedMapComposer(@Nullable @Named("jgroups.config.file") String confFile) {
    this.confFile = confFile;
  }

  public <K, V> ConcurrentMap<K, V> getOrDefault(
      ConcurrentMap<K, V> initialMap, String clusterName) {
    if (confFile != null) {
      try {
        Channel channel = new JChannel(confFile);
        channel.connect(clusterName);
        ReplicatedHashMap<K, V> map = new ReplicatedHashMap<>(initialMap, channel);
        map.start(5000);
        return map;
      } catch (Exception e) {
        LOG.error("Unable to create distributed map. Default one will be used.", e);
      }
    }
    return initialMap;
  }
}
