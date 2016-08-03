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
package org.eclipse.che.api.agent.server.model.impl;

import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.Objects;

/**
 * @author Anatolii Bazko
 */
public class AgentKeyImpl implements AgentKey {
    private final String fqn;
    private final String version;

    public AgentKeyImpl(String fqn, @Nullable String version) {
        this.fqn = fqn;
        this.version = version;
    }

    public String getFqn() {
        return fqn;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Factory method. Agent key is basically a string meeting the format: {@code fqn:version}.
     * The version part can be omitted.
     *
     * @throws IllegalArgumentException
     *      in case of wrong format
     */
    public static AgentKeyImpl of(String agentKey) throws IllegalArgumentException {
        String[] parts = agentKey.split(":");

        if (parts.length == 1) {
            return new AgentKeyImpl(parts[0], null);
        } else if (parts.length == 2) {
            return new AgentKeyImpl(parts[0], parts[1]);
        } else {
            throw new IllegalArgumentException("" + agentKey);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentKeyImpl)) return false;
        AgentKeyImpl agentKey = (AgentKeyImpl)o;
        return Objects.equals(fqn, agentKey.fqn) &&
               Objects.equals(version, agentKey.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fqn, version);
    }

    @Override
    public String toString() {
        return fqn + (version != null ? ":" + version : "");
    }
}
