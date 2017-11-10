/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.openshift.client.kubernetes;

import java.util.Arrays;
import org.eclipse.che.plugin.openshift.client.OpenShiftConnector;

/**
 * Holder class for metadata about an exec, to be used with {@link OpenShiftConnector}.
 *
 * <p>In OpenShift, {@code createExec()} is not separate from {@code startExec()}, so this class has
 * to be used to pass data between {@code createExec()} and {@code startExec()} calls.
 *
 * @see OpenShiftConnector#createExec(org.eclipse.che.plugin.docker.client.params.CreateExecParams)
 * @see OpenShiftConnector#startExec(org.eclipse.che.plugin.docker.client.params.StartExecParams,
 *     org.eclipse.che.plugin.docker.client.MessageProcessor)
 */
public class KubernetesExecHolder {

  private String[] command;
  private String podName;
  private String containerId;

  public KubernetesExecHolder withCommand(String[] command) {
    this.command = command;
    return this;
  }

  public KubernetesExecHolder withPod(String podName) {
    this.podName = podName;
    return this;
  }

  public KubernetesExecHolder withContainerId(String containerId) {
    this.containerId = containerId;
    return this;
  }

  public String[] getCommand() {
    return command;
  }

  public String getPod() {
    return podName;
  }

  public String getContainerId() {
    return containerId;
  }

  public String toString() {
    return String.format(
        "KubernetesExecHolder {command=%s, podName=%s}",
        Arrays.asList(command).toString(), podName);
  }
}
