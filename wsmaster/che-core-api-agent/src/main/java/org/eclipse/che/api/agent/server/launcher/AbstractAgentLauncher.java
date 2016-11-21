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
package org.eclipse.che.api.agent.server.launcher;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * Launch agent script asynchronously over target instance and wait when it run.
 * The policy of checking if agent is run might be different for agents.
 *
 * @see Agent#getScript()
 * @see AgentLaunchingChecker
 * @see AgentLaunchingChecker#DEFAULT
 *
 * @author Anatolii Bazko
 */
public abstract class AbstractAgentLauncher implements AgentLauncher {
    private static final Logger          LOG      = LoggerFactory.getLogger(AbstractAgentLauncher.class);
    private static final ExecutorService executor =
            Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("AgentLauncher-%d")
                                                                    .setDaemon(true)
                                                                    .build());

    private final AgentLaunchingChecker agentLaunchingChecker;
    private final long                  agentPingDelayMs;
    private final long                  agentMaxStartTimeMs;

    public AbstractAgentLauncher(long agentMaxStartTimeMs,
                                 long agentPingDelayMs,
                                 AgentLaunchingChecker agentLaunchingChecker) {
        this.agentPingDelayMs = agentPingDelayMs;
        this.agentMaxStartTimeMs = agentMaxStartTimeMs;
        this.agentLaunchingChecker = agentLaunchingChecker;
    }

    @Override
    public void launch(Instance machine, Agent agent) throws ServerException {
        if (isNullOrEmpty(agent.getScript())) {
            return;
        }
        try {
            final InstanceProcess process = start(machine, agent);
            LOG.debug("Waiting for agent {} is launched. Workspace ID:{}", agent.getId(), machine.getWorkspaceId());

            final long pingStartTimestamp = System.currentTimeMillis();
            while (System.currentTimeMillis() - pingStartTimestamp < agentMaxStartTimeMs) {
                if (agentLaunchingChecker.isLaunched(agent, process, machine)) {
                    return;
                } else {
                    Thread.sleep(agentPingDelayMs);
                }
            }

            process.kill();
        } catch (MachineException e) {
            throw new ServerException(e.getServiceError());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerException(format("Launching agent %s is interrupted", agent.getName()));
        }

        final String errMsg = format("Fail launching agent %s. Workspace ID:%s", agent.getName(), machine.getWorkspaceId());
        LOG.error(errMsg);
        throw new ServerException(errMsg);
    }


    protected InstanceProcess start(final Instance machine, final Agent agent) throws ServerException {
        final Command command = new CommandImpl(agent.getId(), agent.getScript(), "agent");
        final InstanceProcess process = machine.createProcess(command, null);
        final LineConsumer lineConsumer = new AbstractLineConsumer() {
            @Override
            public void writeLine(String line) throws IOException {
                machine.getLogger().writeLine(line);
            }
        };

        executor.execute(ThreadLocalPropagateContext.wrap(() -> {
            try {
                process.start(lineConsumer);
            } catch (ConflictException | MachineException e) {
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

        return process;
    }
}
