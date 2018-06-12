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
package org.eclipse.che.api.deploy;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import java.util.Map;
import org.eclipse.che.api.core.notification.RemoteSubscriptionStorage;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.workspace.server.WorkspaceLockService;
import org.eclipse.che.api.workspace.server.WorkspaceStatusCache;
import org.eclipse.che.multiuser.api.distributed.JGroupsServiceTermination;
import org.eclipse.che.multiuser.api.distributed.WorkspaceStopPropagator;
import org.eclipse.che.multiuser.api.distributed.subscription.DistributedRemoteSubscriptionStorage;
import org.eclipse.persistence.config.CacheCoordinationProtocol;
import org.eclipse.persistence.config.PersistenceUnitProperties;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
public class ReplicationModule extends AbstractModule {

  private static final String JGROUPS_CONF_FILE = "jgroups/che-tcp.xml";

  private Map<String, String> persistenceProperties;

  public ReplicationModule(Map<String, String> persistenceProperties) {
    this.persistenceProperties = persistenceProperties;
  }

  @Override
  protected void configure() {
    // Replication stuff
    persistenceProperties.put(
        PersistenceUnitProperties.COORDINATION_PROTOCOL, CacheCoordinationProtocol.JGROUPS);
    persistenceProperties.put(
        PersistenceUnitProperties.COORDINATION_JGROUPS_CONFIG, JGROUPS_CONF_FILE);
    bindConstant().annotatedWith(Names.named("jgroups.config.file")).to(JGROUPS_CONF_FILE);

    bind(RemoteSubscriptionStorage.class).to(DistributedRemoteSubscriptionStorage.class);

    bind(WorkspaceLockService.class)
        .to(org.eclipse.che.multiuser.api.distributed.lock.JGroupsWorkspaceLockService.class);
    bind(WorkspaceStatusCache.class)
        .to(org.eclipse.che.multiuser.api.distributed.cache.JGroupsWorkspaceStatusCache.class);

    Multibinder.newSetBinder(binder(), ServiceTermination.class)
        .addBinding()
        .to(JGroupsServiceTermination.class);

    bind(WorkspaceStopPropagator.class).asEagerSingleton();
  }
}
