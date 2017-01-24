/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.machine.ssh;

import com.google.inject.assistedinject.Assisted;
import com.jcraft.jsch.JSch;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.plugin.machine.ssh.jsch.JschSshClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Set;

/**
 * @author Max Shaposhnik
 */
public class SshMachineFactoryImpl implements SshMachineFactory {

    private final int connectionTimeoutMs;
    private final Set<ServerConf> machinesServers;

    @Inject
    public  SshMachineFactoryImpl(@Named("che.workspace.ssh_connection_timeout_ms") int connectionTimeoutMs,
                                  @Named("machine.ssh.machine_servers") Set<ServerConf> machinesServers) {
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.machinesServers = machinesServers;
    }


    @Override
    public SshClient createSshClient(@Assisted SshMachineRecipe sshMachineRecipe, @Assisted Map<String, String> envVars) {
        return new JschSshClient(sshMachineRecipe, envVars, new JSch(), connectionTimeoutMs);
    }

    @Override
    public SshMachineInstance createInstance(@Assisted Machine machine, @Assisted SshClient sshClient,
                                             @Assisted LineConsumer outputConsumer) throws MachineException {
        return new SshMachineInstance(machine, sshClient, outputConsumer, this, machinesServers);
    }

    @Override
    public SshMachineProcess createInstanceProcess(@Assisted Command command, @Assisted("outputChannel") String outputChannel,
                                                   @Assisted int pid, @Assisted SshClient sshClient) {
        return new SshMachineProcess(command, outputChannel, pid, sshClient);
    }
}
