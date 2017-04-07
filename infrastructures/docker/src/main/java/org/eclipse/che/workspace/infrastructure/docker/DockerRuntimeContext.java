package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.NotSupportedException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.ValidationException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Alexander Garagatyi
 */
// TODO
// workspace start interruption
// snapshots
// backups
// containers monitoring
public class DockerRuntimeContext extends RuntimeContext {
    private final DockerEnvironment      dockerEnvironment;
    private final List<String>           orderedServices;
    private final Queue<String>          startQueue;
    private final CopyOnWriteArrayList   machines;
    private final DockerNetworkLifecycle dockerNetworkLifecycle;
    private final DockerServiceStarter   serviceStarter;

    public DockerRuntimeContext(DockerEnvironment dockerEnvironment,
                                Environment environment,
                                RuntimeIdentity identity,
                                RuntimeInfrastructure infrastructure,
                                URL registryEndpoint,
                                List<String> orderedServices, DockerNetworkLifecycle dockerNetworkLifecycle,
                                DockerServiceStarter serviceStarter)
            throws ValidationException,
                   ApiException,
                   IOException {
        super(environment, identity, infrastructure, registryEndpoint);
        this.dockerEnvironment = dockerEnvironment;
        this.orderedServices = orderedServices;
        this.dockerNetworkLifecycle = dockerNetworkLifecycle;
        this.serviceStarter = serviceStarter;
        this.startQueue = new ArrayDeque<>();
        this.machines = new CopyOnWriteArrayList<>();
    }

    @Override
    protected InternalRuntime internalStart(Map<String, String> startOptions) throws ServerException {
        // https://github.com/eclipse/che/blob/master/wsmaster/che-core-api-workspace/src/main/java/org/eclipse/che/api/environment/server/CheEnvironmentEngine.java#L728
        // peek machine
        // start each machine
        // start agents in each machine
        // peek machine
        dockerNetworkLifecycle.createNetwork(dockerEnvironment.getNetwork());
        String machine = queuePeek();
        while (machine != null) {
            DockerService service = getService(machine);
            MachineRuntime machineRuntime = serviceStarter.startService(dockerEnvironment.getNetwork(),
                                                                        service,
                                                                        logger);
            startedHandler.started(machineRuntime, agents);
            machine = queuePeek();
        }

        return null;
    }

    @Override
    protected void internalStop(Map<String, String> stopOptions) throws ServerException {

    }


    @Override
    public URL getOutputChannel() throws NotSupportedException, ServerException {
        throw new NotSupportedException();
    }
}
