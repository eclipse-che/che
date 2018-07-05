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
