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

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;

/**
 * TODO Add docs TODO Add TCK
 *
 * @author Sergii Leshchenko
 */
public interface KubernetesMachineCache {

  void add(RuntimeIdentity runtimeIdentity, KubernetesMachineImpl machine)
      throws InfrastructureException;

  boolean updateServerStatus(
      RuntimeIdentity runtimeIdentity, String machineName, String serverRef, ServerStatus status)
      throws InfrastructureException;

  ServerImpl getServer(RuntimeIdentity runtimeIdentity, String machineName, String serverName)
      throws InfrastructureException;

  void updateMachineStatus(RuntimeIdentity runtimeIdentity, String name, MachineStatus status)
      throws InfrastructureException;

  Map<String, KubernetesMachineImpl> getMachines(RuntimeIdentity runtimeIdentity)
      throws InfrastructureException;

  void delete(RuntimeIdentity identity) throws InfrastructureException;
}
