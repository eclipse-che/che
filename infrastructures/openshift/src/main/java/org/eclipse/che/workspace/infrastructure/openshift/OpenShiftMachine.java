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
package org.eclipse.che.workspace.infrastructure.openshift;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;

/** @author Sergii Leshchenko */
public class OpenShiftMachine implements Machine {
  private static final String OPENSHIFT_POD_STATUS_RUNNING = "Running";
  // TODO Make timeout configurable
  private static final int EXEC_TIMEOUT_MIN = 5;

  private final String machineName;
  private final String podName;
  private final String containerName;
  private final Map<String, String> attributes;
  private final Map<String, ServerImpl> ref2Server;
  private final OpenShiftProject project;

  private MachineStatus status;

  public OpenShiftMachine(
      String machineName,
      String podName,
      String containerName,
      Map<String, ServerImpl> ref2Server,
      OpenShiftProject project,
      MachineStatus status,
      Map<String, String> attributes) {
    this.machineName = machineName;
    this.podName = podName;
    this.containerName = containerName;
    if (ref2Server != null) {
      this.ref2Server = ImmutableMap.copyOf(ref2Server);
    } else {
      this.ref2Server = Collections.emptyMap();
    }
    if (attributes != null) {
      this.attributes = ImmutableMap.copyOf(attributes);
    } else {
      this.attributes = Collections.emptyMap();
    }
    this.project = project;
    this.status = status;
  }

  public String getName() {
    return machineName;
  }

  public String getPodName() {
    return podName;
  }

  public String getContainerName() {
    return containerName;
  }

  @Override
  public Map<String, String> getAttributes() {
    return attributes;
  }

  @Override
  public Map<String, ? extends Server> getServers() {
    return ref2Server;
  }

  @Override
  public MachineStatus getStatus() {
    return status;
  }

  public void setStatus(MachineStatus status) {
    this.status = status;
  }

  void setServerStatus(String serverRef, ServerStatus status) {
    ServerImpl server = ref2Server.get(serverRef);
    if (server == null) {
      throw new IllegalArgumentException(
          "Server with provided reference " + serverRef + " is missed");
    }
    server.setStatus(status);
  }

  public void exec(String... command) throws InfrastructureException {
    project.pods().exec(podName, containerName, EXEC_TIMEOUT_MIN, command);
  }

  public void waitRunning(int timeoutMin) throws InfrastructureException {
    project
        .pods()
        .wait(
            podName,
            timeoutMin,
            p -> (OPENSHIFT_POD_STATUS_RUNNING.equals(p.getStatus().getPhase())));
  }
}
