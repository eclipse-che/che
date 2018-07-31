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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import com.google.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Adds to each machine inside environment volume with logs root.
 *
 * @author Anton Korneta
 */
public class LogsVolumeMachineProvisioner implements ConfigurationProvisioner {
  public static final String LOGS_VOLUME_NAME = "che-logs";

  private final String logsRootPath;

  @Inject
  public LogsVolumeMachineProvisioner(@Named("che.workspace.logs.root_dir") String logsRootPath) {
    this.logsRootPath = logsRootPath;
  }

  @Override
  public void provision(KubernetesEnvironment environment, RuntimeIdentity identity)
      throws InfrastructureException {
    for (InternalMachineConfig machine : environment.getMachines().values()) {
      machine.getVolumes().put(LOGS_VOLUME_NAME, new VolumeImpl().withPath(logsRootPath));
    }
  }
}
