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

import org.eclipse.che.api.agent.shared.model.AgentConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Anatolii Bazko
 */
public class SimpleAgent extends AbstractAgent {

    public SimpleAgent(AgentConfig agentConfig) {
        super(agentConfig);
    }

    @Override
    public Map<String, String> getEnvVariables() {
        return Collections.emptyMap();
    }

    @Override
    public List<String> getVolumes() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getPorts() {
        return Collections.emptyList();
    }
}
