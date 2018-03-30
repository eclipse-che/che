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
package org.eclipse.che.workspace.infrastructure.kubernetes.cache.machine;

import static org.mockito.Mockito.mock;

import com.google.inject.TypeLiteral;
import org.eclipse.che.commons.test.db.H2DBTestServer;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.db.PersistTestModuleBuilder;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.h2.jpa.eclipselink.H2ExceptionHandler;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStatusesCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.JpaKubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity.KubernetesMachineEntity;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity.KubernetesRuntimeEntity;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity.KubernetesServerEntity;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.h2.Driver;

/** @author Sergii Leshchenko */
public class JpaTckModule extends TckModule {

  @Override
  protected void configure() {
    H2DBTestServer server = H2DBTestServer.startDefault();
    install(
        new PersistTestModuleBuilder()
            .setDriver(Driver.class)
            .runningOn(server)
            .addEntityClasses(
                KubernetesRuntimeEntity.class,
                KubernetesRuntimeEntity.Id.class,
                KubernetesMachineEntity.class,
                KubernetesMachineEntity.KubernetesMachineId.class,
                KubernetesServerEntity.class,
                KubernetesServerEntity.KubernetesServerId.class)
            .setExceptionHandler(H2ExceptionHandler.class)
            .build());

    bind(new TypeLiteral<TckRepository<KubernetesRuntimeEntity>>() {})
        .toInstance(new JpaTckRepository<>(KubernetesRuntimeEntity.class));

    bind(new TypeLiteral<TckRepository<KubernetesMachineEntity>>() {})
        .toInstance(new JpaTckRepository<>(KubernetesMachineEntity.class));
    bind(KubernetesNamespaceFactory.class).toInstance(mock(KubernetesNamespaceFactory.class));

    bind(KubernetesRuntimeStatusesCache.class).to(JpaKubernetesRuntimeStateCache.class);

    bind(new TypeLiteral<TckRepository<KubernetesServerEntity>>() {})
        .toInstance(new JpaTckRepository<>(KubernetesServerEntity.class));

    bind(SchemaInitializer.class)
        .toInstance(new FlywaySchemaInitializer(server.getDataSource(), "che-schema"));
    bind(DBInitializer.class).asEagerSingleton();
    bind(TckResourcesCleaner.class).toInstance(new H2JpaCleaner(server));
  }
}
