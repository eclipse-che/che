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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.workspace.server.WorkspaceServiceTermination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Termination for Kubernetes HTTP client.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class KubernetesClientTermination implements ServiceTermination {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesClientTermination.class);

  private KubernetesClientFactory kubernetesClientFactory;

  @Inject
  public KubernetesClientTermination(KubernetesClientFactory factory) {
    this.kubernetesClientFactory = factory;
  }

  @Override
  public void terminate() throws InterruptedException {
    suspend();
  }

  @Override
  public void suspend() throws InterruptedException {
    try {
      kubernetesClientFactory.shutdownClient();
    } catch (RuntimeException e) {
      LOG.error(e.getMessage());
    }
  }

  @Override
  public String getServiceName() {
    return "KubernetesClient";
  }

  @Override
  public Set<String> getDependencies() {
    return ImmutableSet.of(WorkspaceServiceTermination.SERVICE_NAME);
  }
}
