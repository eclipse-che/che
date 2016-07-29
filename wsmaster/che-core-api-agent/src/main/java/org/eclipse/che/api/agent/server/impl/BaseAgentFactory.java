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

import com.google.inject.Singleton;

import org.eclipse.che.api.agent.server.Agent;
import org.eclipse.che.api.agent.server.AgentException;
import org.eclipse.che.api.agent.server.AgentFactory;
import org.eclipse.che.api.agent.shared.model.AgentConfig;

/**
 * @author Anatolii Bazko
 */
@Singleton
public class BaseAgentFactory implements AgentFactory {
    @Override
    public String getFqn() {
        return "org.eclipse.che.base";
    }

    @Override
    public Agent create(AgentConfig agentConfig) throws AgentException {
        return new BaseAgent(agentConfig);
    }
}
