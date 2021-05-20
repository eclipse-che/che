/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.commons.observability.ExecutorServiceWrapper;

/**
 * Provides single {@link ExecutorService} instance with daemon threads for Kubernetes/Openshfit
 * infrastructures components.
 *
 * @author Anton Korneta
 */
@Singleton
public class KubernetesSharedPool {

  private final ExecutorService executor;

  @Inject
  public KubernetesSharedPool(ExecutorServiceWrapper executorServiceWrapper) {
    final ThreadFactory factory =
        new ThreadFactoryBuilder()
            .setNameFormat("KubernetesMachineSharedPool-%d")
            .setDaemon(true)
            .build();
    this.executor =
        executorServiceWrapper.wrap(
            Executors.newCachedThreadPool(factory), KubernetesSharedPool.class.getName());
  }

  public ExecutorService getExecutor() {
    return executor;
  }
}
