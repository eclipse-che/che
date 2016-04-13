/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.machine.ssh;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.api.machine.server.terminal.MachineImplSpecificTerminalLauncher;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Launch websocket terminal in ssh machines.
 *
 * @author Alexander Garagatyi
 */
public class SshMachineImplTerminalLauncher implements MachineImplSpecificTerminalLauncher {
    private static final Logger LOG = getLogger(SshMachineImplTerminalLauncher.class);

    public static final String TERMINAL_LAUNCH_COMMAND_PROPERTY = "machine.ssh.server.terminal.run_command";
    public static final String TERMINAL_LOCATION_PROPERTY       = "machine.ssh.server.terminal.location";

    private final String                             runTerminalCommand;
    private final String                             terminalLocation;
    private final WebsocketTerminalFilesPathProvider archivePathProvider;

    @Inject
    public SshMachineImplTerminalLauncher(@Named(TERMINAL_LAUNCH_COMMAND_PROPERTY) String runTerminalCommand,
                                          @Named(TERMINAL_LOCATION_PROPERTY) String terminalLocation,
                                          WebsocketTerminalFilesPathProvider terminalPathProvider) {
        this.runTerminalCommand = runTerminalCommand;
        this.terminalLocation = terminalLocation;
        this.archivePathProvider = terminalPathProvider;
    }

    @Override
    public String getMachineType() {
        return "ssh";
    }

    // todo stop outdated terminal
    // todo check existing version of terminal, do not copy if it is up to date
    @Override
    public void launchTerminal(Instance machine) throws MachineException {
        try {
            InstanceProcess checkTerminalAlive = machine.createProcess(
                    new CommandImpl("check if che websocket terminal is running",
                                    "ps ax | grep 'che-websocket-terminal' | grep -q -v 'grep che-websocket-terminal' && echo 'found' || echo 'not found'",
                                    null),
                    null);
            ListLineConsumer lineConsumer = new ListLineConsumer();
            checkTerminalAlive.start(lineConsumer);
            String checkAliveText = lineConsumer.getText();

            if ("not found".equals(checkAliveText)) {
                machine.copy(archivePathProvider.getPath(firstNonNull(machine.getConfig().getArchitecture(), "linux_amd64")),
                             terminalLocation);

                InstanceProcess startTerminal = machine.createProcess(new CommandImpl("websocket terminal",
                                                                                      runTerminalCommand,
                                                                                      null),
                                                                      null);

                startTerminal.start(new LineConsumer() {
                    @Override
                    public void writeLine(String line) throws IOException {
                        machine.getLogger().writeLine("[Terminal] " + line);
                    }

                    @Override
                    public void close() throws IOException {}
                });
            } else if (!"found".equals(checkAliveText)) {
                LOG.error("Unexpected output of websocket terminal check. Output:" + checkAliveText);
            }
        } catch (ConflictException e) {
            // should never happen
            throw new MachineException("Internal server error occurs on terminal launching.");
        }
    }
}
