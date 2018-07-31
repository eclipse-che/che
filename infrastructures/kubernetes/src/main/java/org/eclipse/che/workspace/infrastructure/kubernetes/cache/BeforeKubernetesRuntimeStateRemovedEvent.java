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
package org.eclipse.che.workspace.infrastructure.kubernetes.cache;

import org.eclipse.che.core.db.cascade.event.RemoveEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;

/**
 * Published before {@link KubernetesRuntimeState kubernetes runtime state} removed.
 *
 * @author Sergii Leshchenko
 */
public class BeforeKubernetesRuntimeStateRemovedEvent extends RemoveEvent {

  private final KubernetesRuntimeState k8sRuntimeState;

  public BeforeKubernetesRuntimeStateRemovedEvent(KubernetesRuntimeState k8sRuntimeState) {
    this.k8sRuntimeState = k8sRuntimeState;
  }

  public KubernetesRuntimeState getRuntimeState() {
    return k8sRuntimeState;
  }
}
