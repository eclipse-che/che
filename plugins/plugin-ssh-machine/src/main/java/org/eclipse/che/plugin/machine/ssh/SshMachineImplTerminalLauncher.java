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

import org.eclipse.che.api.agent.server.terminal.WebsocketTerminalFilesPathProvider;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.api.workspace.server.launcher.TerminalAgentLauncherImpl;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Launch websocket terminal in ssh machines.
 *
 * @author Alexander Garagatyi
 * @author Anatolii Bazko
 */
public class SshMachineImplTerminalLauncher extends TerminalAgentLauncherImpl {
    private static final Logger LOG = getLogger(SshMachineImplTerminalLauncher.class);

    // Regex to parse output of command 'uname -sm'
    // Consists of:
    // 1. named group 'os' that contains 1+ non-space characters
    // 2. space character
    // 3. named group 'architecture' that contains 1+ non-space characters
    private static final Pattern UNAME_OUTPUT         = Pattern.compile("\\[STDOUT\\] (?<os>[\\S]+) (?<architecture>[\\S]+)");
    private static final String  DEFAULT_ARCHITECTURE = "linux_amd64";

    private final WebsocketTerminalFilesPathProvider archivePathProvider;
    private final String                             terminalLocation;

    @Inject
    public SshMachineImplTerminalLauncher(@Named("che.agent.dev.max_start_time_ms") long agentMaxStartTimeMs,
                                          @Named("che.agent.dev.ping_delay_ms") long agentPingDelayMs,
                                          @Named("machine.ssh.server.terminal.location") String terminalLocation,
                                          @Named("machine.terminal_agent.run_command") String terminalRunCommand,
                                          WebsocketTerminalFilesPathProvider terminalPathProvider) {
        super(agentMaxStartTimeMs, agentPingDelayMs, terminalRunCommand);
        this.archivePathProvider = terminalPathProvider;
        this.terminalLocation = terminalLocation;
    }

    @Override
    public String getMachineType() {
        return "ssh";
    }

    @Override
    public void launch(Instance machine, Agent agent) throws ServerException {
        try {
            String architecture = detectArchitecture(machine);
            machine.copy(archivePathProvider.getPath(architecture), terminalLocation);

            super.launch(machine, agent);
        } catch (ConflictException e) {
            // should never happen
            throw new ServerException("Internal server error occurs on terminal launching.");
        }
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
}
