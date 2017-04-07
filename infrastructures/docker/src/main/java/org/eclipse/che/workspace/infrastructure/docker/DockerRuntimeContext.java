package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.NotSupportedException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.ValidationException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
public class DockerRuntimeContext extends RuntimeContext {
    private final DockerEnvironment         dockerEnvironment;
    private final List<String>              orderedServices;

    public DockerRuntimeContext(DockerEnvironment dockerEnvironment,
                                Environment environment,
                                RuntimeIdentity identity,
                                RuntimeInfrastructure infrastructure,
                                URL registryEndpoint,
                                List<String> orderedServices)
            throws ValidationException,
                   ApiException,
                   IOException {
        super(environment, identity, infrastructure, registryEndpoint);
        this.dockerEnvironment = dockerEnvironment;
        this.orderedServices = orderedServices;
    }

    @Override
    protected InternalRuntime internalStart(Map<String, String> startOptions) throws ServerException {


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
