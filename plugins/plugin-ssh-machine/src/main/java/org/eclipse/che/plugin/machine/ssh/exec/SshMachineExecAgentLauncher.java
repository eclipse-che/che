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
package org.eclipse.che.plugin.machine.ssh.exec;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.agent.server.terminal.WebsocketTerminalFilesPathProvider;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.impl.AgentImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.eclipse.che.plugin.machine.ssh.SshMachineInstance;
import org.eclipse.che.plugin.machine.ssh.SshMachineProcess;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Launch exec agent in ssh machines.
 *
 * @author Alexander Garagatyi
 * @author Anatolii Bazko
 */
public class SshMachineExecAgentLauncher  {
    private static final Logger LOG = getLogger(SshMachineExecAgentLauncher.class);
    // Regex to parse output of command 'uname -sm'
    // Consists of:
    // 1. named group 'os' that contains 1+ non-space characters
    // 2. space character
    // 3. named group 'architecture' that contains 1+ non-space characters
    private static final Pattern         UNAME_OUTPUT         = Pattern.compile("\\[STDOUT\\] (?<os>[\\S]+) (?<architecture>[\\S]+)");
    private static final String          DEFAULT_ARCHITECTURE = "linux_amd64";
    private static final ExecutorService executor             =
            Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("SshAgentLauncher-%d")
                                                                    .setUncaughtExceptionHandler(
                                                                            LoggingUncaughtExceptionHandler.getInstance())
                                                                    .setDaemon(true)
                                                                    .build());

    private final WebsocketTerminalFilesPathProvider archivePathProvider;
    private final String                             terminalLocation;
    private final long                               agentMaxStartTimeMs;
    private final long                               agentPingDelayMs;
    private final String                             terminalRunCommand;

    @Inject
    public SshMachineExecAgentLauncher(@Named("che.agent.dev.max_start_time_ms") long agentMaxStartTimeMs,
                                       @Named("che.agent.dev.ping_delay_ms") long agentPingDelayMs,
                                       @Named("machine.ssh.server.terminal.location") String terminalLocation,
                                       @Named("machine.terminal_agent.run_command") String terminalRunCommand,
                                       WebsocketTerminalFilesPathProvider terminalPathProvider) {
        this.agentMaxStartTimeMs = agentMaxStartTimeMs;
        this.agentPingDelayMs = agentPingDelayMs;
        this.terminalRunCommand = terminalRunCommand;
        this.archivePathProvider = terminalPathProvider;
        this.terminalLocation = terminalLocation;
    }

    public void launch(SshMachineInstance machine, Agent agent) throws ServerException {
        if (isNullOrEmpty(agent.getScript())) {
            return;
        }
        try {
            String architecture = detectArchitecture(machine);
            machine.copy(archivePathProvider.getPath(architecture), terminalLocation);
            final AgentImpl agentCopy = new AgentImpl(agent);
            agentCopy.setScript(agent.getScript() + "\n" + terminalRunCommand);

            final SshMachineProcess process = start(machine, agentCopy);
            LOG.debug("Waiting for agent {} is launched. Workspace ID:{}", agentCopy.getId(), machine.getWorkspaceId());

            final long pingStartTimestamp = System.currentTimeMillis();
            SshProcessLaunchedChecker agentLaunchingChecker = new SshProcessLaunchedChecker("che-websocket-terminal");
            while (System.currentTimeMillis() - pingStartTimestamp < agentMaxStartTimeMs) {
                if (agentLaunchingChecker.isLaunched(agentCopy, machine)) {
                    return;
                } else {
                    Thread.sleep(agentPingDelayMs);
                }
            }

            process.kill();

            final String errMsg = format("Fail launching agent %s. Workspace ID:%s", agent.getName(), machine.getWorkspaceId());
            LOG.error(errMsg);
            throw new ServerException(errMsg);
        } catch (ServerException e) {
            throw new ServerException(e.getServiceError());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerException(format("Launching agent %s is interrupted", agent.getName()));
        } catch (ConflictException e) {
            // should never happen
            throw new ServerException("Internal server error occurs on terminal launching.");
        }
    }

    private String detectArchitecture(SshMachineInstance machine) throws ConflictException, ServerException {
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
        SshMachineProcess getUnameOutput = machine.createProcess(new CommandImpl("discover machine architecture",
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

    protected SshMachineProcess start(SshMachineInstance machine, Agent agent) throws ServerException {
        Command command = new CommandImpl(agent.getId(), agent.getScript(), "agent");
        SshMachineProcess process = machine.createProcess(command, null);
        LineConsumer lineConsumer = new AbstractLineConsumer() {
            @Override
            public void writeLine(String line) throws IOException {
                machine.getLogger().writeLine(line);
            }
        };

        CountDownLatch countDownLatch = new CountDownLatch(1);
        executor.execute(ThreadLocalPropagateContext.wrap(() -> {
            try {
                countDownLatch.countDown();
                process.start(lineConsumer);
            } catch (ConflictException e) {
                try {
                    machine.getLogger().writeLine(format("[ERROR] %s", e.getMessage()));
                } catch (IOException ignored) {
                }
            } finally {
                try {
                    lineConsumer.close();
                } catch (IOException ignored) {
                }
            }
        }));
        try {
            // ensure that code inside of task submitted to executor is called before end of this method
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return process;
    }
}
