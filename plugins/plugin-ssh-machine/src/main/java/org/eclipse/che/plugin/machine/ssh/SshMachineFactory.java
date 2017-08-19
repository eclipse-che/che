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
package org.eclipse.che.plugin.machine.ssh;

import com.jcraft.jsch.JSch;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.plugin.machine.ssh.jsch.JschSshClient;

/**
 * Provides ssh machine implementation instances.
 *
 * @author Alexander Garagatyi
 * @author Max Shaposhnik
 */
public class SshMachineFactory {

  private final int connectionTimeoutMs;
  private final Set<ServerConf> machinesServers;

  @Inject
  public SshMachineFactory(
      @Named("che.workspace.ssh_connection_timeout_ms") int connectionTimeoutMs,
      @Named("machine.ssh.machine_servers") Set<ServerConf> machinesServers) {
    this.connectionTimeoutMs = connectionTimeoutMs;
    this.machinesServers = machinesServers;
  }

  /**
   * Creates {@link SshClient} to communicate with machine over SSH protocol.
   *
   * @param sshMachineRecipe recipe of machine
   * @param envVars environment variables that should be injected into machine
   */
  public SshClient createSshClient(SshMachineRecipe sshMachineRecipe, Map<String, String> envVars) {
    return new JschSshClient(sshMachineRecipe, envVars, new JSch(), connectionTimeoutMs);
  }

  /**
   * Creates ssh machine implementation instance.
   *
   * @param machine description of machine
   * @param sshClient ssh client of machine
   * @param outputConsumer consumer of output from container main process
   * @throws MachineException if error occurs on creation of {@code Instance}
   */
  public SshMachineInstance createInstance(
      Machine machine, SshClient sshClient, LineConsumer outputConsumer) throws MachineException {
    return new SshMachineInstance(machine, sshClient, outputConsumer, this, machinesServers);
  }

  /**
   * Creates ssh machine implementation of {@link SshMachineProcess}.
   *
   * @param command command that should be executed on process start
   * @param outputChannel channel where output will be available on process execution
   * @param pid virtual id of that process
   * @param sshClient client to communicate with machine
   */
  public SshMachineProcess createInstanceProcess(
      Command command, String outputChannel, int pid, SshClient sshClient) {
    return new SshMachineProcess(command, outputChannel, pid, sshClient);
  }
}
