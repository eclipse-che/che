/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper;

import com.google.inject.assistedinject.Assisted;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.workspace.infrastructure.kubernetes.StartSynchronizer;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;

/** @author Sergii Leshchenko */
public interface KubernetesBootstrapperFactory {
  KubernetesBootstrapper create(
      @Assisted RuntimeIdentity runtimeIdentity,
      @Assisted List<? extends Installer> agents,
      @Assisted KubernetesMachineImpl kubernetesMachine,
      @Assisted KubernetesNamespace namespace,
      @Assisted StartSynchronizer startSynchronizer);
}
