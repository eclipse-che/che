package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;

import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
public class DockerMachine implements Machine {
    @Override
    public Map<String, String> getProperties() {
        return null;
    }

    @Override
    public Map<String, ? extends Server> getServers() {
        return null;
    }
}
