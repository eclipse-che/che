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
package org.eclipse.che.api.agent.server.impl;

import com.google.common.base.Joiner;

import org.eclipse.che.api.agent.server.Agent;
import org.eclipse.che.api.agent.shared.model.AgentConfig;

import java.util.List;

/**
 * @author Anatolii Bazko
 */
public abstract class AbstractAgent implements Agent {
    protected final AgentConfig agentConfig;

    public AbstractAgent(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
    }

    @Override
    public final String getFqn() {
        return agentConfig.getFqn();
    }

    @Override
    public String getVersion() {
        return agentConfig.getVersion();
    }

    @Override
    public List<String> getDependencies() {
        return agentConfig.getDependencies();
    }

    @Override
    public String getScript() {
        return Joiner.on('\n').join(agentConfig.getScript());
    }
}

