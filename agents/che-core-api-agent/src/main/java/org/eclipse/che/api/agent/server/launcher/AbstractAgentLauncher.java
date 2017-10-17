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
package org.eclipse.che.api.agent.server.launcher;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.che.api.agent.server.exception.AgentStartException;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launch agent script asynchronously over target instance and wait when it run. The policy of
 * checking if agent is run might be different for agents.
 *
 * @see Agent#getScript()
 * @see AgentLaunchingChecker
 * @see AgentLaunchingChecker#DEFAULT
 * @author Anatolii Bazko
 */
public abstract class AbstractAgentLauncher implements AgentLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractAgentLauncher.class);
  private static final ExecutorService executor =
      Executors.newCachedThreadPool(
          new ThreadFactoryBuilder()
              .setNameFormat("AgentLauncher-%d")
              .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
              .setDaemon(true)
              .build());

  private final AgentLaunchingChecker agentLaunchingChecker;
  private final long agentPingDelayMs;
  private final long agentMaxStartTimeMs;

  public AbstractAgentLauncher(
      long agentMaxStartTimeMs,
      long agentPingDelayMs,
      AgentLaunchingChecker agentLaunchingChecker) {
    this.agentPingDelayMs = agentPingDelayMs;
    this.agentMaxStartTimeMs = agentMaxStartTimeMs;
    this.agentLaunchingChecker = agentLaunchingChecker;
  }

  @Override
  public void launch(Instance machine, Agent agent) throws ServerException, AgentStartException {
    if (isNullOrEmpty(agent.getScript())) {
      return;
    }
    ListLineConsumer agentLogger = new ListLineConsumer();
    LineConsumer lineConsumer =
        new AbstractLineConsumer() {
          @Override
          public void writeLine(String line) throws IOException {
            machine.getLogger().writeLine(line);
            agentLogger.writeLine(line);
          }
        };
    try {
      final InstanceProcess process = start(machine, agent, lineConsumer);
      LOG.debug(
          "Waiting for agent {} is launched. Workspace ID:{}",
          agent.getId(),
          machine.getWorkspaceId());

      final long pingStartTimestamp = System.currentTimeMillis();
      while (System.currentTimeMillis() - pingStartTimestamp < agentMaxStartTimeMs) {
        if (agentLaunchingChecker.isLaunched(agent, process, machine)) {
          return;
        } else {
          Thread.sleep(agentPingDelayMs);
        }
      }
      LOG.error(
          format(
              "Fail launching agent '%s' in '%s' workspace due to timeout",
              agent.getName(), machine.getWorkspaceId()));

      if (this.shouldBlockMachineStartOnError()) {
        LOG.info("Stopping workspace {}", machine.getWorkspaceId());
        process.kill();
      } else {
        LOG.info(
            "Continuing workspace bootstrap even if agent {} failed to start.", agent.getName());
        return;
      }
    } catch (MachineException e) {
      logAsErrorAgentStartLogs(machine, agent.getName(), agentLogger.getText());
      throw new ServerException(e.getServiceError());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ServerException(format("Launching agent %s is interrupted", agent.getName()));
    } finally {
      try {
        lineConsumer.close();
      } catch (IOException ignored) {
      }
      agentLogger.close();
    }

    logAsErrorAgentStartLogs(machine, agent.getName(), agentLogger.getText());
    throw new AgentStartException(
        format(
            "Fail launching agent %s. Workspace ID:%s", agent.getName(), machine.getWorkspaceId()));
  }

  protected InstanceProcess start(Instance machine, Agent agent, LineConsumer lineConsumer)
      throws ServerException {
    Command command = new CommandImpl(agent.getId(), agent.getScript(), "agent");
    InstanceProcess process = machine.createProcess(command, null);

    CountDownLatch countDownLatch = new CountDownLatch(1);
    executor.execute(
        ThreadLocalPropagateContext.wrap(
            () -> {
              try {
                countDownLatch.countDown();
                process.start(lineConsumer);
              } catch (ConflictException | MachineException e) {
                try {
                  machine.getLogger().writeLine(format("[ERROR] %s", e.getMessage()));
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

  @VisibleForTesting
  void logAsErrorAgentStartLogs(Instance machine, String agentName, String logs) {
    if (!logs.isEmpty()) {
      LOG.error(
          "An error occurs while starting '{}' agent in '{}' workspace in '{}' machine on '{}' node. Detailed log:\n{}",
          agentName,
          machine.getWorkspaceId(),
          machine.getId(),
          machine.getNode().getHost(),
          logs);
    } else {
      LOG.error(
          "An error occurs while starting '{}' agent in '{}' workspace in '{}' machine on '{}' node. "
              + "The agent didn't produce any logs.",
          agentName,
          machine.getWorkspaceId(),
          machine.getId(),
          machine.getNode().getHost());
    }
  }

  @Override
  public boolean shouldBlockMachineStartOnError() {
    return true;
  }
}
