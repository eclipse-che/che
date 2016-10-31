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
package org.eclipse.che.api.workspace.server.launcher;

import org.eclipse.che.api.agent.server.launcher.AbstractAgentLauncher;
import org.eclipse.che.api.agent.server.launcher.ProcessIsLaunchedChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Starts terminal agent.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class SshAgentLauncherImpl extends AbstractAgentLauncher {
    protected static final Logger LOG = LoggerFactory.getLogger(SshAgentLauncherImpl.class);

    @Inject
    public SshAgentLauncherImpl(@Named("che.agent.dev.max_start_time_ms") long agentMaxStartTimeMs,
                                @Named("che.agent.dev.ping_delay_ms") long agentPingDelayMs) {
        super(agentMaxStartTimeMs, agentPingDelayMs, new ProcessIsLaunchedChecker("sshd"));
    }

    @Override
    public String getMachineType() {
        return "docker";
    }

    @Override
    public String getAgentId() {
        return "org.eclipse.che.ssh";
    }
}
