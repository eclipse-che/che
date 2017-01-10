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

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;

import java.util.Map;

/**
 * Provides ssh machine implementation instances.
 *
 * @author Alexander Garagatyi
 */
public interface SshMachineFactory {

    /**
     * Creates {@link SshClient} to communicate with machine over SSH protocol.
     *
     * @param sshMachineRecipe
     *         recipe of machine
     * @param envVars
     *         environment variables that should be injected into machine
     */
    SshClient createSshClient(@Assisted SshMachineRecipe sshMachineRecipe,
                              @Assisted Map<String, String> envVars);

    /**
     * Creates ssh machine implementation of {@link Instance}.
     *
     * @param machine description of machine
     * @param sshClient ssh client of machine
     * @param outputConsumer consumer of output from container main process
     * @throws MachineException if error occurs on creation of {@code Instance}
     */
    SshMachineInstance createInstance(@Assisted Machine machine,
                                      @Assisted SshClient sshClient,
                                      @Assisted LineConsumer outputConsumer) throws MachineException;

    /**
     * Creates ssh machine implementation of {@link org.eclipse.che.api.machine.server.spi.InstanceProcess}.
     *
     * @param command command that should be executed on process start
     * @param outputChannel channel where output will be available on process execution
     * @param pid virtual id of that process
     * @param sshClient client to communicate with machine
     */
    SshMachineProcess createInstanceProcess(@Assisted Command command,
                                            @Assisted("outputChannel") String outputChannel,
                                            @Assisted int pid,
                                            @Assisted SshClient sshClient);
}
