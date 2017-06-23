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
package org.eclipse.che.api.agent.server.model.impl;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.Objects;

/**
 * @author Anatolii Bazko
 */
public class AgentKeyImpl implements AgentKey {
    private static final String DEFAULT_VERSION = "latest";
    private final String id;
    private final String version;

    public AgentKeyImpl(String id, @Nullable String version) {
        this.id = id;
        this.version = version == null ? DEFAULT_VERSION : version;
    }

    public AgentKeyImpl(String id) {
        this(id, null);
    }

    public AgentKeyImpl(Agent agent) {
        this(agent.getId(), agent.getVersion());
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Factory method. Agent key is basically a string meeting the format: {@code id:version}.
     * The version part can be omitted.
     *
     * @throws IllegalArgumentException
     *      in case of wrong format
     */
    public static AgentKeyImpl parse(String agentKey) throws IllegalArgumentException {
        String[] parts = agentKey.split(":");

        if (parts.length == 1) {
            return new AgentKeyImpl(parts[0]);
        } else if (parts.length == 2) {
            return new AgentKeyImpl(parts[0], parts[1]);
        } else {
            throw new IllegalArgumentException("Illegal format: " + agentKey);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentKeyImpl)) return false;
        AgentKeyImpl agentKey = (AgentKeyImpl)o;
        return Objects.equals(id, agentKey.id) &&
               Objects.equals(version, agentKey.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    public String asString() {
        return id + (version != null ? ":" + version : "");
    }

    @Override
    public String toString() {
        return "AgentImpl{" +
               "id='" + id + '\'' +
               ", version='" + version + "\'}";
    }
}
