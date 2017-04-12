package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
public class DockerInternalRuntime extends InternalRuntime {
    private final Map<String, DockerMachine> machines;
    private final Map<String, String> properties;

    public DockerInternalRuntime(RuntimeContext context,
                                 URLRewriter urlRewriter,
                                 Map<String, DockerMachine> machines) {
        super(context, urlRewriter);
        this.machines = machines;
        this.properties = new HashMap<>();
    }

    @Override
    public Map<String, ? extends Machine> getInternalMachines() {
        return Collections.unmodifiableMap(machines);
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}
