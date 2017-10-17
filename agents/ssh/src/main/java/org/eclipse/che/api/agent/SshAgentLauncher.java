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
package org.eclipse.che.api.agent;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.inject.Named;
import org.eclipse.che.api.agent.server.launcher.AbstractAgentLauncher;
import org.eclipse.che.api.agent.server.launcher.SshAgentLaunchingChecker;

/**
 * Starts SSH agent.
 *
 * @author Anatolii Bazko
 * @author Alexander Garagatyi
 */
@Singleton
public class SshAgentLauncher extends AbstractAgentLauncher {
  @Inject
  public SshAgentLauncher(
      @Named("che.agent.dev.max_start_time_ms") long agentMaxStartTimeMs,
      @Named("che.agent.dev.ping_delay_ms") long agentPingDelayMs) {
    super(agentMaxStartTimeMs, agentPingDelayMs, new SshAgentLaunchingChecker());
  }

  @Override
  public String getMachineType() {
    return "docker";
  }

  @Override
  public String getAgentId() {
    return "org.eclipse.che.ssh";
  }

  @Override
  public boolean shouldBlockMachineStartOnError() {
    return false;
  }
}
