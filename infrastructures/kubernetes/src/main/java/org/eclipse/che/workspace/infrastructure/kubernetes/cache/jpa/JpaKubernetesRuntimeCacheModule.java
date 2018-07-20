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
package org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa;

import com.google.inject.AbstractModule;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;

/** @author Sergii Leshchenko */
public class JpaKubernetesRuntimeCacheModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(KubernetesRuntimeStateCache.class).to(JpaKubernetesRuntimeStateCache.class);
    bind(KubernetesMachineCache.class).to(JpaKubernetesMachineCache.class);
    bind(JpaKubernetesRuntimeStateCache.RemoveKubernetesRuntimeBeforeWorkspaceRemoved.class)
        .asEagerSingleton();
    bind(JpaKubernetesMachineCache.RemoveKubernetesMachinesBeforeRuntimesRemoved.class)
        .asEagerSingleton();
  }
}
