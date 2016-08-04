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
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.api.machine.server.terminal.MachineImplSpecificTerminalLauncher;
import org.eclipse.che.api.machine.server.terminal.WebsocketTerminalFilesPathProvider;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Launch websocket terminal in ssh machines.
 *
 * @author Alexander Garagatyi
 */
public class SshMachineImplTerminalLauncher implements MachineImplSpecificTerminalLauncher {
    private static final Logger LOG = getLogger(SshMachineImplTerminalLauncher.class);

    // Regex to parse output of command 'uname -sm'
    // Consists of:
    // 1. named group 'os' that contains 1+ non-space characters
    // 2. space character
    // 3. named group 'architecture' that contains 1+ non-space characters
    private static final Pattern UNAME_OUTPUT         = Pattern.compile("\\[STDOUT\\] (?<os>[\\S]+) (?<architecture>[\\S]+)");
    private static final String  DEFAULT_ARCHITECTURE = "linux_amd64";

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
            if (!isWebsocketTerminalRunning(machine)) {
                String architecture = detectArchitecture(machine);

                machine.copy(archivePathProvider.getPath(architecture),
                             terminalLocation);

                startTerminal(machine);
            }
        } catch (ConflictException e) {
            // should never happen
            throw new MachineException("Internal server error occurs on terminal launching.");
        }
    }

    private boolean isWebsocketTerminalRunning(Instance machine) throws MachineException, ConflictException {
        InstanceProcess checkTerminalAlive = machine.createProcess(
                new CommandImpl("check if che websocket terminal is running",
                                "ps ax | grep 'che-websocket-terminal' | grep -q -v 'grep che-websocket-terminal' && echo 'found' || echo 'not found'",
                                null),
                null);
        ListLineConsumer lineConsumer = new ListLineConsumer();
        checkTerminalAlive.start(lineConsumer);
        String checkAliveText = lineConsumer.getText();
        if ("[STDOUT] not found".equals(checkAliveText)) {
            return false;
        } else if (!"[STDOUT] found".equals(checkAliveText)) {
            LOG.error("Unexpected output of websocket terminal check. Output:" + checkAliveText);
            return false;
        }
        return true;
    }

    private String detectArchitecture(Instance machine) throws ConflictException, MachineException {
        // uname -sm shows OS and CPU architecture
        // Examples of output:
        // Windows 10 amd64
        // MSYS_NT-6.3 x86_64
        // (empty line)
        // Ubuntu Linux 14.04 amd64
        // Linux x86_64
        // OS X amd64
        // Darwin x86_64
        // Samsung Artik arm7
        // Linux armv7l
        InstanceProcess getUnameOutput = machine.createProcess(new CommandImpl("discover machine architecture",
                                                                               "uname -sm",
                                                                               null),
                                                               null);
        ListLineConsumer lineConsumer = new ListLineConsumer();
        getUnameOutput.start(lineConsumer);
        String unameOutput = lineConsumer.getText();
        Matcher matcher = UNAME_OUTPUT.matcher(unameOutput);
        if (matcher.matches()) {
            String os = matcher.group("os").toLowerCase();
            String arch = matcher.group("architecture").toLowerCase();
            StringBuilder result = new StringBuilder();
            if (os.contains("linux")) {
                result.append("linux_");
            } else if (os.contains("darwin")) {
                result.append("darwin_");
            } else if (os.contains("msys")) {
                result.append("windows_");
            } else {
                LOG.error(format("Architecture discovering fails. Machine %s. uname output:%s", machine.getId(), unameOutput));
                return DEFAULT_ARCHITECTURE;
            }
            if (arch.contains("x86_64")) {
                result.append("amd64");
            } else if (arch.contains("armv7l")) {
                result.append("arm7");
            } else if (arch.contains("armv6l")) {
                result.append("arm6");
            } else if (arch.contains("armv5l")) {
                result.append("arm5");
            } else {
                LOG.error(format("Architecture discovering fails. Machine %s. uname output:%s", machine.getId(), unameOutput));
                return DEFAULT_ARCHITECTURE;
            }

            return result.toString();
        } else {
            LOG.error(format("Architecture discovering fails. Machine %s. uname output:%s", machine.getId(), unameOutput));
            return DEFAULT_ARCHITECTURE;
        }
    }

    private void startTerminal(Instance machine) throws MachineException, ConflictException {
        InstanceProcess startTerminal = machine.createProcess(new CommandImpl("websocket terminal",
                                                                              runTerminalCommand,
                                                                              null),
                                                              null);

        startTerminal.start(new AbstractLineConsumer() {
            @Override
            public void writeLine(String line) throws IOException {
                machine.getLogger().writeLine("[Terminal] " + line);
            }
        });
    }
}
